import os
import tempfile
import unittest
import sys

# Add the utils directory to the path so we can import validate_snippets
sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))

import validate_snippets

class TestValidateSnippets(unittest.TestCase):
    def setUp(self):
        self.test_dir = tempfile.TemporaryDirectory()
        self.docs_dir = self.test_dir.name
        
    def tearDown(self):
        self.test_dir.cleanup()

    def create_mock_doc(self, filename, content):
        path = os.path.join(self.docs_dir, filename)
        with open(path, 'w', encoding='utf-8') as f:
            f.write(content)
        return path

    def test_is_skipped_file_no_frontmatter(self):
        path = self.create_mock_doc('no_frontmatter.md', '# Title\n\nSome text.')
        self.assertFalse(validate_snippets.is_skipped_file(path))

    def test_is_skipped_file_public_visibility(self):
        content = "---\nvisibility: public\n---\n# Title\n"
        path = self.create_mock_doc('public.md', content)
        self.assertFalse(validate_snippets.is_skipped_file(path))

    def test_is_skipped_file_draft_visibility(self):
        content = "---\nvisibility: draft\n---\n# Title\n"
        path = self.create_mock_doc('draft.md', content)
        self.assertTrue(validate_snippets.is_skipped_file(path))

    def test_is_skipped_file_restricted_visibility_quoted(self):
        content = "---\nvisibility: \"restricted\"\n---\n# Title\n"
        path = self.create_mock_doc('restricted.md', content)
        self.assertTrue(validate_snippets.is_skipped_file(path))

    def test_is_skipped_file_case_insensitive(self):
        content = "---\nVisIbiLiTy: 'DrAfT'\n---\n# Title\n"
        path = self.create_mock_doc('draft_case.md', content)
        self.assertTrue(validate_snippets.is_skipped_file(path))

    def test_validation_skips_draft(self):
        content = "---\nvisibility: draft\n---\n```json\n{\n  \"invalid\": \"json\"\n```\n"
        path = self.create_mock_doc('invalid_draft.md', content)
        self.assertTrue(validate_snippets.is_skipped_file(path))
        # Since it's skipped in main(), the validation function wouldn't be called,
        # but let's test validate_snippets itself still catches errors if manually called
        errors = validate_snippets.validate_snippets(path)
        self.assertTrue(len(errors) > 0)

if __name__ == '__main__':
    unittest.main()
