let originalMessage = '';

document.addEventListener('DOMContentLoaded', function() {
  const textarea = document.getElementById('inviteMessage');
  const charCount = document.getElementById('charCount');
  const previewContent = document.getElementById('previewContent');
  
  // 초기 메시지 저장
  originalMessage = textarea.value;
  
  // 초기 글자 수 설정
  updateCharCount();
  updatePreview();
  
  // 텍스트 영역 이벤트 리스너
  textarea.addEventListener('input', function() {
    updateCharCount();
    updatePreview();
  });
  
  // 폼 제출 이벤트
  document.getElementById('inviteMessageForm').addEventListener('submit', function(e) {
    e.preventDefault();
    saveInviteMessage();
  });
});

// 글자 수 업데이트
function updateCharCount() {
  const textarea = document.getElementById('inviteMessage');
  const charCount = document.getElementById('charCount');
  const currentLength = textarea.value.length;
  const maxLength = 500;
  
  charCount.textContent = currentLength;
  
  // 글자 수에 따른 스타일 변경
  charCount.parentElement.classList.remove('warning', 'danger');
  if (currentLength > maxLength * 0.8) {
    charCount.parentElement.classList.add('warning');
  }
  if (currentLength > maxLength) {
    charCount.parentElement.classList.add('danger');
  }
}

// 미리보기 업데이트
function updatePreview() {
  const textarea = document.getElementById('inviteMessage');
  const previewContent = document.getElementById('previewContent');
  
  const message = textarea.value.trim();
  previewContent.textContent = message || '메시지를 입력하면 여기에 미리보기가 표시됩니다.';
}

// 메시지 초기화
function resetMessage() {
  if (confirm('입력한 내용을 모두 삭제하고 원래 메시지로 되돌리시겠습니까?')) {
    const textarea = document.getElementById('inviteMessage');
    textarea.value = originalMessage;
    updateCharCount();
    updatePreview();
  }
}

// 초대 메시지 저장
function saveInviteMessage() {
  const textarea = document.getElementById('inviteMessage');
  const message = textarea.value.trim();
  const channelId = document.getElementById('channelMeta').dataset.channelId;
  
  if (!message) {
    alert('초대 메시지를 입력해주세요.');
    return;
  }
  
  if (message.length > 500) {
    alert('메시지는 500자를 초과할 수 없습니다.');
    return;
  }
  
  // 버튼 비활성화
  const saveBtn = document.querySelector('.save-btn');
  saveBtn.disabled = true;
  saveBtn.textContent = '저장 중...';
  
  fetch(`/admin/channels/${channelId}/invite-message`, {
    method: 'PATCH',
    headers: {
      'Content-Type': 'application/json'
    },
    body: JSON.stringify({
      message: message
    })
  })
  .then(response => {
    if (!response.ok) {
      throw new Error('저장에 실패했습니다.');
    }
    return response;
  })
  .then(data => {
    // 성공 메시지 표시
    alert('초대 메시지가 성공적으로 저장되었습니다.');
    
    // 원본 메시지 업데이트
    originalMessage = message;
  })
  .catch(error => {
    console.error('Error:', error);
    alert('저장 중 오류가 발생했습니다: ' + error.message);
  })
  .finally(() => {
    // 버튼 복원
    saveBtn.disabled = false;
    saveBtn.textContent = '메시지 수정';
  });
}
