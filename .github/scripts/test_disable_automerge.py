import os
import json
import subprocess
import pytest
from unittest.mock import patch, MagicMock

import disable_automerge

@patch('subprocess.run')
def test_run_command_success(mock_run):
    mock_result = MagicMock()
    mock_result.returncode = 0
    mock_result.stdout = '{"success": true}'
    mock_run.return_value = mock_result

    output = disable_automerge.run_command(['gh', 'api', '...'])
    assert output == '{"success": true}'
    assert mock_run.call_count == 1

@patch('subprocess.run')
@patch('time.sleep', return_value=None)
def test_run_command_retry_success(mock_sleep, mock_run):
    mock_fail = MagicMock()
    mock_fail.returncode = 1
    mock_fail.stderr = "Error"
    
    mock_success = MagicMock()
    mock_success.returncode = 0
    mock_success.stdout = "Success"
    
    mock_run.side_effect = [mock_fail, mock_success]

    output = disable_automerge.run_command(['gh', 'api', '...'])
    assert output == "Success"
    assert mock_run.call_count == 2
    assert mock_sleep.call_count == 1

@patch('subprocess.run')
@patch('time.sleep', return_value=None)
def test_run_command_retry_fail(mock_sleep, mock_run):
    mock_fail = MagicMock()
    mock_fail.returncode = 1
    mock_fail.stderr = "Error"
    mock_run.return_value = mock_fail

    with pytest.raises(Exception, match="Command failed: gh api ..."):
        disable_automerge.run_command(['gh', 'api', '...'], max_retries=2)
    
    assert mock_run.call_count == 2
    assert mock_sleep.call_count == 1

@patch('disable_automerge.get_event_data')
@patch('disable_automerge.get_pr_files')
@patch('disable_automerge.check_automerge_status')
@patch('disable_automerge.disable_automerge')
@patch('disable_automerge.get_existing_comments')
@patch('disable_automerge.post_comment')
@patch.dict(os.environ, {'GITHUB_TOKEN': 'fake_token'})
def test_main_with_core_files(mock_post_comment, mock_get_comments, mock_disable_automerge_api, mock_check_automerge, mock_get_pr_files, mock_get_event_data):
    mock_get_event_data.return_value = {
        'pull_request': {
            'number': 123,
            'node_id': 'PR_1',
            'base': {'repo': {'full_name': 'test/repo'}}
        }
    }
    
    # Only core files
    mock_get_pr_files.return_value = ['core/src/main/java/App.java', 'docs/readme.md']
    mock_check_automerge.return_value = True
    mock_get_comments.return_value = []
    
    disable_automerge.main()
    
    mock_disable_automerge_api.assert_called_once_with('PR_1')
    mock_post_comment.assert_called_once()
    
    args, _ = mock_post_comment.call_args
    assert "core backend module files" in args[2]
    assert "database schema migration files" not in args[2]

@patch('disable_automerge.get_event_data')
@patch('disable_automerge.get_pr_files')
@patch('disable_automerge.check_automerge_status')
@patch('disable_automerge.disable_automerge')
@patch('disable_automerge.get_existing_comments')
@patch('disable_automerge.post_comment')
@patch.dict(os.environ, {'GITHUB_TOKEN': 'fake_token'})
def test_main_with_db_files(mock_post_comment, mock_get_comments, mock_disable_automerge_api, mock_check_automerge, mock_get_pr_files, mock_get_event_data):
    mock_get_event_data.return_value = {
        'pull_request': {
            'number': 123,
            'node_id': 'PR_1',
            'base': {'repo': {'full_name': 'test/repo'}}
        }
    }
    
    # DB migration files
    mock_get_pr_files.return_value = ['core/src/main/resources/migration/v1/changelog.xml']
    mock_check_automerge.return_value = True
    mock_get_comments.return_value = []
    
    disable_automerge.main()
    
    mock_disable_automerge_api.assert_called_once_with('PR_1')
    mock_post_comment.assert_called_once()
    
    args, _ = mock_post_comment.call_args
    assert "database schema migration files" in args[2]
    assert "core backend module files" not in args[2]

@patch('disable_automerge.get_event_data')
@patch('disable_automerge.get_pr_files')
@patch('disable_automerge.check_automerge_status')
@patch('disable_automerge.disable_automerge')
@patch('disable_automerge.get_existing_comments')
@patch('disable_automerge.post_comment')
@patch.dict(os.environ, {'GITHUB_TOKEN': 'fake_token'})
def test_main_already_commented(mock_post_comment, mock_get_comments, mock_disable_automerge_api, mock_check_automerge, mock_get_pr_files, mock_get_event_data):
    mock_get_event_data.return_value = {
        'pull_request': {
            'number': 123,
            'node_id': 'PR_1',
            'base': {'repo': {'full_name': 'test/repo'}}
        }
    }
    
    mock_get_pr_files.return_value = ['core/src/main/resources/migration/v1/changelog.xml']
    mock_check_automerge.return_value = True
    mock_get_comments.return_value = [{'body': '<!-- auto-merge-disabler-signal --> \n Warning'}]
    
    disable_automerge.main()
    
    mock_disable_automerge_api.assert_called_once_with('PR_1')
    mock_post_comment.assert_not_called()

@patch('disable_automerge.get_event_data')
@patch('disable_automerge.get_pr_files')
@patch('disable_automerge.check_automerge_status')
@patch('disable_automerge.disable_automerge')
@patch('disable_automerge.get_existing_comments')
@patch('disable_automerge.post_comment')
@patch.dict(os.environ, {'GITHUB_TOKEN': 'fake_token'})
def test_main_automerge_not_active(mock_post_comment, mock_get_comments, mock_disable_automerge_api, mock_check_automerge, mock_get_pr_files, mock_get_event_data):
    mock_get_event_data.return_value = {
        'pull_request': {
            'number': 123,
            'node_id': 'PR_1',
            'base': {'repo': {'full_name': 'test/repo'}}
        }
    }
    
    mock_get_pr_files.return_value = ['core/src/main/resources/migration/v1/changelog.xml']
    mock_check_automerge.return_value = False
    mock_get_comments.return_value = []
    
    disable_automerge.main()
    
    mock_disable_automerge_api.assert_not_called()
    mock_post_comment.assert_called_once()
