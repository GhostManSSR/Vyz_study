"""End-to-end tests for lab 10 (Python 3 stdlib only)."""
import os
from pathlib import Path
import re
import subprocess
import tempfile
import unittest

ROOT = Path(__file__).resolve().parents[1]


class PGrepTests(unittest.TestCase):
    @classmethod
    def setUpClass(cls):
        cls.build = tempfile.TemporaryDirectory(prefix="pgrep_build_")
        cls.exe = Path(cls.build.name) / ("pgrep.exe" if os.name == "nt" else "pgrep")
        subprocess.run(
            ["g++", "-std=c++17", "-O2", "-Wall", "-Wextra", "-pthread",
             str(ROOT / "main.cpp"), "-o", str(cls.exe)],
            check=True, capture_output=True, text=True,
        )

    @classmethod
    def tearDownClass(cls):
        cls.build.cleanup()

    def setUp(self):
        self.tmp = tempfile.TemporaryDirectory(prefix="pgrep_test_")
        self.root = Path(self.tmp.name)

    def tearDown(self):
        self.tmp.cleanup()

    def run_pgrep(self, *args):
        return subprocess.run(
            [str(self.exe), *map(str, args)],
            capture_output=True, text=True, encoding="utf-8", errors="replace",
            timeout=15,
        )

    def write(self, name, data):
        p = self.root / name
        p.parent.mkdir(parents=True, exist_ok=True)
        p.write_bytes(data.encode("utf-8") if isinstance(data, str) else data)
        return p

    def test_fixed_substring_and_line_numbers(self):
        p = self.write("a.txt", "foo bar\nbaz\nfoo\n")
        r = self.run_pgrep("-n", "foo", p)
        self.assertEqual(r.returncode, 0)
        self.assertEqual(r.stdout, "1:foo bar\n3:foo\n")
        self.assertRegex(r.stderr, r"Processed: 1 files, 2 matches, 0 errors, \d+\.\d+ s")

    def test_last_line_without_newline_and_empty_file(self):
        p = self.write("a.txt", "first\nlast foo")
        r = self.run_pgrep("-n", "foo", p)
        self.assertEqual(r.stdout, "2:last foo\n")
        empty = self.write("empty.txt", "")
        self.assertEqual(self.run_pgrep("-c", "foo", empty).stdout, f"{empty}:0\n")
        self.assertEqual(self.run_pgrep("foo", empty).returncode, 1)

    def test_ascii_case_insensitive(self):
        p = self.write("a.txt", "Error\nERROR\nerror\nother\n")
        r = self.run_pgrep("-ic", "error", p)
        self.assertEqual(r.stdout, f"{p}:3\n")
        self.assertEqual(r.returncode, 0)

    def test_word_boundaries_and_search_later_occurrence(self):
        p = self.write("a.txt", "catapult cat\nconcatenate\na cat sat\ncat_3\n-cat!\n")
        r = self.run_pgrep("-w", "cat", p)
        self.assertEqual(r.stdout, "catapult cat\na cat sat\n-cat!\n")

    def test_full_line_empty_pattern_and_empty_line(self):
        p = self.write("a.txt", "foo\nfoo bar\n\n")
        self.assertEqual(self.run_pgrep("-x", "foo", p).stdout, "foo\n")
        self.assertEqual(self.run_pgrep("-x", "", p).stdout, "\n")
        self.assertEqual(self.run_pgrep("", p).stdout, "foo\nfoo bar\n\n")

    def test_invert_applies_before_count_and_list(self):
        p = self.write("a.txt", "foo\nbar\nfoo\n")
        self.assertEqual(self.run_pgrep("-v", "foo", p).stdout, "bar\n")
        self.assertEqual(self.run_pgrep("-vc", "foo", p).stdout, f"{p}:1\n")
        self.assertEqual(self.run_pgrep("-vl", "foo", p).stdout, f"{p}\n")

    def test_duplicate_matches_single_line(self):
        p = self.write("a.txt", "foo foo foo\n")
        self.assertEqual(self.run_pgrep("-c", "foo", p).stdout, f"{p}:1\n")

    def test_filename_rules_and_precedence(self):
        a = self.write("a.txt", "foo\n")
        b = self.write("b.txt", "foo\n")
        self.assertEqual(self.run_pgrep("foo", a).stdout, "foo\n")
        self.assertEqual(self.run_pgrep("-H", "foo", a).stdout, f"{a}:foo\n")
        self.assertEqual(self.run_pgrep("-Hh", "foo", a).stdout, "foo\n")
        self.assertEqual(self.run_pgrep("-hH", "foo", a).stdout, f"{a}:foo\n")
        self.assertCountEqual(self.run_pgrep("foo", a, b).stdout.splitlines(),
                              [f"{a}:foo", f"{b}:foo"])
        self.assertCountEqual(self.run_pgrep("-h", "foo", a, b).stdout.splitlines(),
                              ["foo", "foo"])
        self.assertEqual(self.run_pgrep("-ch", "foo", a).stdout, "1\n")

    def test_recursive_and_no_symlink_follow(self):
        a = self.write("sub/a.txt", "foo\n")
        b = self.write("sub/nested/b.txt", "foo\n")
        link = self.root / "sub" / "other.txt"
        try:
            link.symlink_to(b)
        except (NotImplementedError, OSError):
            pass
        r = self.run_pgrep("-rn", "foo", self.root)
        self.assertEqual(r.returncode, 0)
        self.assertCountEqual(r.stdout.splitlines(), [f"{a}:1:foo", f"{b}:1:foo"])
        self.assertIn("Processed: 2 files, 2 matches, 0 errors", r.stderr)

    def test_directory_without_recursive_does_not_block_other_paths(self):
        p = self.write("a.txt", "foo\n")
        r = self.run_pgrep("foo", self.root, p)
        self.assertEqual(r.returncode, 2)
        self.assertEqual(r.stdout, f"{p}:foo\n")
        self.assertIn("is a directory", r.stderr)
        self.assertIn("Processed: 2 files, 1 matches, 1 errors", r.stderr)

    def test_invalid_file_does_not_block_others(self):
        p = self.write("a.txt", "foo\n")
        missing = self.root / "missing.txt"
        r = self.run_pgrep("foo", missing, p)
        self.assertEqual(r.returncode, 2)
        self.assertEqual(r.stdout, f"{p}:foo\n")
        self.assertIn("2 files, 1 matches, 1 errors", r.stderr)

    def test_binary_file_is_error_without_partial_output(self):
        good = self.write("good.txt", "foo\n")
        corrupt = self.write("bad.txt", b"foo\nabc\0def\n")
        r = self.run_pgrep("foo", corrupt, good)
        self.assertEqual(r.returncode, 2)
        self.assertEqual(r.stdout, f"{good}:foo\n")
        self.assertIn("binary/corrupted file", r.stderr)
        self.assertIn("Processed: 2 files, 1 matches, 1 errors", r.stderr)

    def test_quiet_suppresses_both_output_channels(self):
        good = self.write("good.txt", "foo\n")
        bad = self.root / "missing.txt"
        hit = self.run_pgrep("-q", "foo", good, bad)
        self.assertEqual((hit.returncode, hit.stdout, hit.stderr), (0, "", ""))
        miss = self.run_pgrep("-q", "bar", good)
        self.assertEqual((miss.returncode, miss.stdout, miss.stderr), (1, "", ""))
        error = self.run_pgrep("-q", "foo", bad)
        self.assertEqual((error.returncode, error.stdout, error.stderr), (2, "", ""))

    def test_double_dash_and_bad_options(self):
        p = self.write("a.txt", "-foo\n")
        self.assertEqual(self.run_pgrep("--", "-foo", p).stdout, "-foo\n")
        self.assertEqual(self.run_pgrep("-j", "0", "foo", p).returncode, 2)
        self.assertEqual(self.run_pgrep("-j", "bad", "foo", p).returncode, 2)
        self.assertEqual(self.run_pgrep("-Z", "foo", p).returncode, 2)
        self.assertEqual(self.run_pgrep().returncode, 2)

    def test_sequential_and_parallel_agree_even_with_many_files(self):
        for i in range(40):
            self.write(f"sub/{i:03d}.txt", f"skip\nneedle {i}\nneedle\n")
        seq = self.run_pgrep("-rnj", "1", "needle", self.root)
        par = self.run_pgrep("-rnj", "4", "needle", self.root)
        self.assertEqual(seq.returncode, 0)
        self.assertEqual(par.returncode, 0)
        self.assertCountEqual(seq.stdout.splitlines(), par.stdout.splitlines())
        self.assertIn("Processed: 40 files, 80 matches, 0 errors", seq.stderr)
        self.assertIn("Processed: 40 files, 80 matches, 0 errors", par.stderr)

    def test_paths_with_spaces(self):
        p = self.write("directory with spaces/my file.txt", "foo\n")
        r = self.run_pgrep("-H", "foo", p)
        self.assertEqual(r.stdout, f"{p}:foo\n")


if __name__ == "__main__":
    unittest.main(verbosity=2)
