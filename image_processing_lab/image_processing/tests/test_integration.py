#!/usr/bin/env python3
"""End-to-end tests using only Python's standard library (no Pillow needed)."""
import pathlib
import struct
import subprocess
import sys
import tempfile
import zlib

PNG_MAGIC = b'\x89PNG\r\n\x1a\n'


def chunk(name, content):
    return struct.pack('>I', len(content)) + name + content + struct.pack('>I', zlib.crc32(name + content))


def write_rgba_png(path, width, height, pixels):
    path.parent.mkdir(parents=True, exist_ok=True)
    data = b''.join(b'\x00' + bytes(pixels[y * width * 4:(y + 1) * width * 4])
                    for y in range(height))
    ihdr = struct.pack('>2I5B', width, height, 8, 6, 0, 0, 0)
    path.write_bytes(PNG_MAGIC + chunk(b'IHDR', ihdr) +
                     chunk(b'IDAT', zlib.compress(data)) + chunk(b'IEND', b''))


def read_rgba_png(path):
    data = path.read_bytes()
    assert data[:8] == PNG_MAGIC, f'Not a PNG: {path}'
    pos, width, height, raw_idat = 8, None, None, b''
    while pos < len(data):
        length = struct.unpack_from('>I', data, pos)[0]
        kind = data[pos + 4:pos + 8]
        content = data[pos + 8:pos + 8 + length]
        pos += 12 + length
        if kind == b'IHDR':
            width, height, bit_depth, color_type, _, _, _ = struct.unpack('>2I5B', content)
            assert (bit_depth, color_type) == (8, 6), (bit_depth, color_type)
        if kind == b'IDAT':
            raw_idat += content
        if kind == b'IEND':
            break
    raw = zlib.decompress(raw_idat)
    scanline = width * 4
    previous = bytearray(scanline)
    result = bytearray()
    for y in range(height):
        start = y * (scanline + 1)
        filter_type = raw[start]
        line = bytearray(raw[start + 1:start + 1 + scanline])
        for i in range(scanline):
            a = line[i - 4] if i >= 4 else 0
            b = previous[i]
            c = previous[i - 4] if i >= 4 else 0
            if filter_type == 1:
                predictor = a
            elif filter_type == 2:
                predictor = b
            elif filter_type == 3:
                predictor = (a + b) // 2
            elif filter_type == 4:
                p = a + b - c
                pa, pb, pc = abs(p - a), abs(p - b), abs(p - c)
                predictor = a if pa <= pb and pa <= pc else b if pb <= pc else c
            elif filter_type == 0:
                predictor = 0
            else:
                raise AssertionError(f'Unexpected PNG filter: {filter_type}')
            line[i] = (line[i] + predictor) & 0xFF
        result.extend(line)
        previous = line
    return width, height, bytes(result)


def execute(program, *args, success=True):
    result = subprocess.run([str(program), *(str(x) for x in args)],
                            capture_output=True, text=True, encoding='utf-8', errors='replace')
    if success and result.returncode != 0 or not success and result.returncode == 0:
        raise AssertionError(f'Unexpected return code {result.returncode}\n'
                             f'Command: {args}\nstdout: {result.stdout}\nstderr: {result.stderr}')
    return result


def verify_images(folder, suffix, expected, count):
    files = list(folder.rglob('*_' + suffix + '.png'))
    assert len(files) == count, (suffix, count, files)
    for file in files:
        width, height, output_pixels = read_rgba_png(file)
        assert (width, height) == (2, 1)
        assert output_pixels == expected, (file, list(output_pixels), list(expected))


def main(program):
    with tempfile.TemporaryDirectory(prefix='image-processor-test-') as temp:
        base = pathlib.Path(temp)
        src = base / 'input'
        src.mkdir()
        pixels = bytes([100, 150, 200, 77, 10, 20, 30, 255])
        for i in range(7):
            write_rgba_png(src / f'image_{i}.png', 2, 1, pixels)
        write_rgba_png(src / 'nested' / 'sub' / 'image_7.PNG', 2, 1, pixels)
        (src / 'ignore.txt').write_text('not an image')

        gray_pixels = bytes([140, 140, 140, 77, 18, 18, 18, 255])
        invert_pixels = bytes([155, 105, 55, 77, 245, 235, 225, 255])
        both_pixels = bytes([115, 115, 115, 77, 237, 237, 237, 255])

        single = base / 'single'
        parallel = base / 'parallel'
        result = execute(program, src, single, 'grayscale', 1)
        assert '8 succeeded, 0 failed' in result.stdout
        result = execute(program, '--input', src, '--output', parallel,
                         '--filter', 'grayscale', '--threads', 4, '--quiet')
        assert '8 succeeded, 0 failed' in result.stdout
        verify_images(single, 'grayscale', gray_pixels, 8)
        verify_images(parallel, 'grayscale', gray_pixels, 8)
        assert (parallel / 'nested' / 'sub' / 'image_7.PNG_grayscale.png').is_file()
        for path in single.rglob('*.png'):
            relative = path.relative_to(single)
            assert read_rgba_png(path) == read_rgba_png(parallel / relative)

        inverse = base / 'inverse'
        execute(program, src, inverse, 'invert', 3)
        verify_images(inverse, 'invert', invert_pixels, 8)

        both = src / 'processed'  # output inside input; should not reprocess its own results
        execute(program, src, both, 'both', 2)
        verify_images(both, 'both', both_pixels, 8)
        result = execute(program, src, both, 'both', 2)
        assert 'Images: 8' in result.stdout
        verify_images(both, 'both', both_pixels, 8)

        corrupt = base / 'with_error'
        corrupt.mkdir()
        write_rgba_png(corrupt / 'good.png', 2, 1, pixels)
        (corrupt / 'bad.png').write_bytes(b'not actually an image')
        result = execute(program, corrupt, base / 'partial_results', 'invert', 2, success=False)
        assert '1 succeeded, 1 failed' in result.stdout
        assert '[ERROR]' in result.stderr
        assert (base / 'partial_results' / 'good.png_invert.png').is_file()

        empty = base / 'empty'
        empty.mkdir()
        result = execute(program, empty, base / 'empty_result', 'grayscale')
        assert 'No supported image files' in result.stdout

        execute(program, src, src, 'invert', success=False)
        execute(program, src, base / 'other', 'unsupported', success=False)
        execute(program, src, base / 'other', 'invert', 0, success=False)
        execute(program, src, base / 'other', 'invert', 129, success=False)
        execute(program, '--help')
    print('ALL INTEGRATION TESTS PASSED: output pixels, alpha, nested paths, 1/4 threads, '
          'bad inputs, CLI validation, output-inside-input protection')


if __name__ == '__main__':
    main(pathlib.Path(sys.argv[1]).resolve())
