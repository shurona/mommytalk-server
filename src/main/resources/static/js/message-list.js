let currentEditMessageId = null;

// 수정 모달 열기
function openEditModal(messageId) {
  currentEditMessageId = messageId;
  
  // 현재 메시지 내용 가져오기
  const row = document.querySelector(`button[onclick*="${messageId}"]`).closest('tr');
  const contentCell = row.querySelector('td:nth-child(2)');
  const currentContent = contentCell.getAttribute('data-content');
  
  // 모달에 현재 내용 설정
  document.getElementById('editContent').value = currentContent;
  
  // 모달 표시
  document.getElementById('editModal').style.display = 'flex';
  document.body.style.overflow = 'hidden'; // 스크롤 방지
}

// 수정 모달 닫기
function closeEditModal() {
  document.getElementById('editModal').style.display = 'none';
  document.body.style.overflow = 'auto'; // 스크롤 복원
  currentEditMessageId = null;
}

// 메시지 수정 저장
function updateMessage() {
  if (!currentEditMessageId) return;
  
  const newContent = document.getElementById('editContent').value.trim();
  
  if (!newContent) {
    alert('메시지 내용을 입력해주세요.');
    return;
  }
  
  const channelId = document.getElementById('groupMeta').dataset.channelId;
  
  fetch(`/admin/channels/${channelId}/messages/${currentEditMessageId}`, {
    method: 'PATCH',
    headers: {
      'Content-Type': 'application/json'
    },
    body: JSON.stringify({ content: newContent })
  })
  .then(response => {
    if (response.ok) {
      alert('메시지가 수정되었습니다.');
      closeEditModal();
      window.location.reload();
    } else {
      return response.json().then(data => {
        throw new Error(data.message || '메시지 수정 중 오류가 발생했습니다.');
      });
    }
  })
  .catch(error => {
    alert( error.message);
  });
}

// 메시지 취소 - 현재 편집 중인 메시지 취소
function cancelMessage() {
  if (!currentEditMessageId) {
    alert('취소할 메시지를 찾을 수 없습니다.');
    return;
  }

  const channelId = document.getElementById('groupMeta').dataset.channelId;

  if (!confirm('정말로 이 메시지를 취소하시겠습니까?')) {
    return;
  }

  fetch(`/admin/channels/${channelId}/messages/${currentEditMessageId}/cancel`, {
    method: 'PATCH',
    headers: {
      'Content-Type': 'application/json'
    }
  })
  .then(response => {
    if (response.ok) {
      alert('메시지가 취소되었습니다.');
      closeEditModal();
      window.location.reload();
    } else {
      alert('취소 요청 중 오류가 발생했습니다.');
    }
  })
  .catch(error => {
    console.error('Error:', error);
    alert( error.message || '취소 요청 중 오류가 발생했습니다.');
  });
}

// ESC 키로 모달 닫기
document.addEventListener('keydown', function(event) {
  if (event.key === 'Escape') {
    closeEditModal();
  }
});

// 모달 외부 클릭시 닫기
document.addEventListener('DOMContentLoaded', function() {
  const editModal = document.getElementById('editModal');
  if (editModal) {
    editModal.addEventListener('click', function(event) {
      if (event.target === this) {
        closeEditModal();
      }
    });
  }
});