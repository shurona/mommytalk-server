// 글자 수 선택 및 preview 처리
document.addEventListener('DOMContentLoaded', function () {
  const textarea = document.getElementById('content');
  const preview = document.querySelector('.preview-message');
  const charCount = document.getElementById('charCount');
  
  function updatePreview() {
      const value = textarea.value;
      preview.innerHTML = value
      .replace(/&/g, "&amp;")    // XSS 방지
      .replace(/</g, "&lt;")     // HTML 이스케이프
      .replace(/>/g, "&gt;")
      .replace(/\n/g, "<br>");   // 줄바꿈을 <br>로
      charCount.textContent = `${value.length}/500`;
  } 

  textarea.addEventListener('input', updatePreview);

  // 초기값이 있을 경우 반영
  updatePreview();
});




// 그룹 토글 선택
function updateTargetUI() {
  const target = document.querySelector('input[name="targetType"]:checked').value;
  const includeBox = document.getElementById('includeGroupBox');
  const excludeBox = document.getElementById('excludeGroupBox');
  
  includeBox.style.display = target === 'GROUP' ? 'block' : 'none';
  excludeBox.style.display = target === 'ALL' ? 'block' : 'none';
}

// 페이지 로드 시 초기화
document.addEventListener('DOMContentLoaded', updateTargetUI);


// 드롭다운 토글 함수 수정: event를 인자로 받도록
function toggleDropdown(id, event) {
  event.stopPropagation(); // 내부 클릭은 전파 막기
  const el = document.getElementById(id);
  el.style.display = el.style.display === 'block' ? 'none' : 'block';
}

// 외부 클릭 시 드롭다운 닫기
document.addEventListener('click', function (event) {
  document.querySelectorAll('.dropdown-menu').forEach(function (dropdown) {
      if (!dropdown.contains(event.target) &&
          !event.target.closest('.dropdown-toggle')) {
      dropdown.style.display = 'none';
      }
  });
});


// 체크박스 그룹에 선택된 항목 수 포함.
function updateGroupCount(groupName, counterId) {
  const checkboxes = document.querySelectorAll(`input[name="${groupName}"]:checked`);
    document.getElementById(counterId).textContent = checkboxes.length;
  }
  
  document.addEventListener('DOMContentLoaded', () => {
  updateGroupCount("includeGroup", "includeCount");
  updateGroupCount("excludeGroup", "excludeCount");
  
  document.querySelectorAll('input[name="includeGroup"]').forEach(cb =>
      cb.addEventListener('change', () => updateGroupCount("includeGroup", "includeCount"))
  );
  document.querySelectorAll('input[name="excludeGroup"]').forEach(cb =>
      cb.addEventListener('change', () => updateGroupCount("excludeGroup", "excludeCount"))
  );
});

// 테스트 발송 버튼 클릭시 Post 요청
function sendTestMessage() {
  const content = document.getElementById('content').value;

  fetch('/admin/messages/v1/send/test', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json'
    },
    body: JSON.stringify({
      content,
    })
  })
  .then(response => response.json())
  .then(data => {
    if (data.success) {
      alert('테스트 메시지가 성공적으로 발송되었습니다.');
    } else {
      alert('메시지 발송에 실패했습니다: ' + data.error);
    }
  })
  .catch(error => {
    console.error('Error:', error);
    alert('메시지 발송 중 오류가 발생했습니다.');
  });
}
