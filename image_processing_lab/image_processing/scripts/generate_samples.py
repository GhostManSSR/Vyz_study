#!/usr/bin/env python3
"""Generate demo PNGs without any external Python packages."""
import argparse
import pathlib
import struct
import zlib

PNG = b'\x89PNG\r\n\x1a\n'


def chunk(name, body):
    return struct.pack('>I', len(body)) + name + body + struct.pack('>I', zlib.crc32(name + body))


def create_png(destination, width, height, style, index):
    destination.parent.mkdir(parents=True, exist_ok=True)
    compressor = zlib.compressobj(level=6)
    compressed = []
    for y in range(height):
        row = bytearray(b'\x00')
        for x in range(width):
            if style == 'gradient':
                r = (x * 255 // max(width - 1, 1) + index * 17) % 256
                g = (y * 255 // max(height - 1, 1) + index * 31) % 256
                b = ((x + y + index * 100) // 2) % 256
            elif style == 'checker':
                a = ((x // 20) + (y // 20) + index) % 2
                r, g, b = ((230, 80, 65) if a else (35, 130, 220))
            else:
                r = (x * 7 + y * 3 + index * 29) % 256
                g = (x * 2 + y * 11 + index * 41) % 256
                b = (x * 5 + y * 9 + index * 53) % 256
            row.extend((r, g, b, 255))
        compressed.append(compressor.compress(row))
    compressed.append(compressor.flush())
    ihdr = struct.pack('>2I5B', width, height, 8, 6, 0, 0, 0)
    destination.write_bytes(PNG + chunk(b'IHDR', ihdr) +
                            chunk(b'IDAT', b''.join(compressed)) + chunk(b'IEND', b''))


def main():
    parser = argparse.ArgumentParser(description='Create reproducible PNG samples for image_processor')
    parser.add_argument('--output', type=pathlib.Path, default=pathlib.Path('examples/input'))
    parser.add_argument('--count', type=int, default=3)
    parser.add_argument('--width', type=int, default=160)
    parser.add_argument('--height', type=int, default=120)
    args = parser.parse_args()
    if not (1 <= args.count <= 1000) or not (1 <= args.width <= 8192) or not (1 <= args.height <= 8192):
        parser.error('count must be 1..1000, width and height must be 1..8192')
    if args.count * args.width * args.height > 125_000_000:
        parser.error('too many total pixels; reduce image count or size')
    for i in range(args.count):
        relative = pathlib.Path('nested') / f'example_{i + 1:03d}.png' if i % 3 == 2 else pathlib.Path(f'example_{i + 1:03d}.png')
        path = args.output / relative
        style = ('gradient', 'checker', 'noise')[i % 3]
        create_png(path, args.width, args.height, style, i)
    print(f'Generated {args.count} images in {args.output}')


if __name__ == '__main__':
    main()
