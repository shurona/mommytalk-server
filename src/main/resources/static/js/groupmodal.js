// 모달 열기
function openCreateGroupModal() {
  document.getElementById('createGroupModal').style.display = 'block';
  document.body.style.overflow = 'hidden'; // 스크롤 방지

  // 입력 필드 초기화
  document.getElementById('groupName').value = '';
  document.getElementById('groupDescription').value = '';
  document.getElementById('friendNames').value = '';
  updateCharCount();
}

// 모달 닫기
function closeCreateGroupModal() {
  document.getElementById('createGroupModal').style.display = 'none';
  document.body.style.overflow = 'auto'; // 스크롤 복원
}

// 모달 외부 클릭시 닫기
window.onclick = function(event) {
  const modal = document.getElementById('createGroupModal');
  if (event.target === modal) {
    closeCreateGroupModal();
  }
}

// 글자수 카운트 업데이트
function updateCharCount() {
  const groupName = document.getElementById('groupName');
  const groupDescription = document.getElementById('groupDescription');
  const friendNames = document.getElementById('friendNames');

  const charCounts = document.querySelectorAll('.char-count');

  groupName.addEventListener('input', function() {
    charCounts[0].textContent = `${this.value.length}/20자`;
  });

  groupDescription.addEventListener('input', function() {
    charCounts[1].textContent = `${this.value.length}/500자`;
  });

  friendNames.addEventListener('input', function() {
    charCounts[2].textContent = `${this.value.length}/1200자`;
  });
}

// 페이지 로드시 이벤트 리스너 등록
document.addEventListener('DOMContentLoaded', function() {
  updateCharCount();
});

// ESC 키로 모달 닫기
document.addEventListener('keydown', function(event) {
  if (event.key === 'Escape') {
    closeCreateGroupModal();
  }
});

// form 버튼 클릭 시 form-section 데이터 갖고 오기
document.getElementById('createGroupModal').addEventListener('submit', function(event) {
  event.preventDefault();
  const name = document.getElementById('groupName').value;
  const description = document.getElementById('groupDescription').value;
  const phoneNumbers = document.getElementById('friendNames').value;
  console.log(name, description, phoneNumbers);

  // friendsNames 배열로 변환 및 휴대전화 번호 형식 검증 후 오류 메시지 출력
  const phoneNumberList = phoneNumbers.split('\n');

  // 휴대전화번호 목록이 비어있으면 오류 메시지 출력
  if(phoneNumberList.length === 0) {
    alert('휴대전화번호를 입력해주세요.');
    return;
  }

  // 휴대전화번호 형식 검증
  for (let i = 0; i < phoneNumberList.length; i++) {
    if (!/^\d{3}-\d{4}-\d{4}$/.test(phoneNumberList[i])) {
      alert('휴대전화번호 형식이 올바르지 않습니다.');
      return;
    }
  }

  // /admin/groups post 요청으로 데이터 전송
  fetch('/admin/groups', {
    method: 'POST',
    body: JSON.stringify({ name, description, phoneNumberList }),
    headers: {
      'Content-Type': 'application/json'
    }
  })
  .then(() => {
    window.location.reload();
  })
  .catch(error => console.error('Error:', error));
});

