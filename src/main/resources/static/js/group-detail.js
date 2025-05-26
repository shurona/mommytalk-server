// 친구 추가 모달 열기
function openAddFriendModal() {
  document.getElementById('addFriendModal').style.display = 'flex';
  document.body.style.overflow = 'hidden'; // 스크롤 방지

  // 입력 필드 초기화 (예: textarea 초기화)
  const phoneInput = document.querySelector('#addFriendModal textarea');
  if (phoneInput) {
    phoneInput.value = '';
  }
}

// 친구 추가 모달 닫기
function closeAddFriendModal() {
  document.getElementById('addFriendModal').style.display = 'none';
  document.body.style.overflow = 'auto'; // 스크롤 복원
}

// 모달 외부 클릭 시 닫기
window.onclick = function(event) {
  const createGroupModal = document.getElementById('createGroupModal');
  const addFriendModal = document.getElementById('addFriendModal');

  if (event.target === createGroupModal) {
    closeCreateGroupModal();
  } else if (event.target === addFriendModal) {
    closeAddFriendModal();
  }
}

// 체크 박스 클릭시 전체 선택/해제
document.addEventListener("DOMContentLoaded", function () {
  const masterCheckbox = document.querySelector("thead input[type='checkbox']");
  const checkboxes = document.querySelectorAll("tbody input[name='selectedFriends']");

  masterCheckbox.addEventListener("change", function () {
    checkboxes.forEach(cb => cb.checked = masterCheckbox.checked);
  });
});

// form 버튼 클릭 시 form-section 데이터 갖고 오기
function addPhoneNumbersToGroup() {
  const phoneNumbers = document.getElementById('phoneNumbers').value;

  // 그룹 ID 가져오기
  const groupId = document.getElementById('groupMeta').dataset.groupId;

  console.log(phoneNumbers, groupId);


  // friendsNames 배열로 변환 및 휴대전화 번호 형식 검증 후 오류 메시지 출력
  const phoneNumberList = phoneNumbers.split('\n');

  // 휴대전화번호 목록이 비어있으면 오류 메시지 출력
  if(phoneNumberList.length === 0) {
    alert('휴대전화번호를 입력해주세요.');
    return;
  }

  // 휴대전화번호 형식 검증
  for (let i = 0; i < phoneNumberList.length; i++) {
    phoneNumberList[i] = phoneNumberList[i].trim(); // 공백 제거
    if (!/^\d{3}-\d{4}-\d{4}$/.test(phoneNumberList[i])) {
      alert('휴대전화번호 형식이 올바르지 않습니다.');
      return;
    }
  }

  // /admin/groups post 요청으로 데이터 전송
  fetch(`/admin/groups/${groupId}/users`, {
    method: 'POST',
    body: JSON.stringify({ phoneNumberList }),
    headers: {
      'Content-Type': 'application/json'
    }
  })
  .then(() => {
    window.location.reload();
  })
  .catch(error => console.error('Error:', error));
}

function deleteSelectedFriends() {
  const checkboxes = document.querySelectorAll("tbody input[name='selectedFriends']:checked");
  const groupId = document.getElementById('groupMeta').dataset.groupId;

  // 선택된 친구가 없으면 경고 메시지 출력
  if (checkboxes.length === 0) {
    alert('삭제할 친구를 선택해주세요.');
    return;
  }


  // 선택된 친구의 휴대전화 번호 목록 생성
  const userGroupIds = Array.from(checkboxes).map(cb => cb.value);

  // 삭제 요청 보내기
  fetch(`/admin/groups/${groupId}/users`, {
    method: 'DELETE',
    body: JSON.stringify({ userGroupIds: userGroupIds }),
    headers: {
      'Content-Type': 'application/json'
    }
  })
  .then(() => {
    window.location.reload();
  })
  .catch(error => console.error('Error:', error));
}