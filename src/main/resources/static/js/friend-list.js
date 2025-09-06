function enableEdit(td) {
  // td가 이미 input을 가지고 있다면 아무것도 하지 않음
  if (td.querySelector('input')) {
    return;
  }
  let currentValue = td.getAttribute('data-phone-number');

  // null 값 처리
  if (currentValue === 'null' || currentValue === null || currentValue === undefined) {
    currentValue = '';
  }

  const userId = td.getAttribute('data-user-id');
    td.innerHTML = `<input value="${currentValue}" 
                         onblur="savePhone(this, ${userId})" 
                         />`;
  td.querySelector('input').focus();
}

function savePhone(input, userId) {
  const newPhone = input.value.trim();
  const td = input.parentElement;
  const beforePhone = td.getAttribute('data-phone-number');
    const lineId = td.parentElement.querySelector('td[data-line-id]').getAttribute('data-line-id');

  if (!/^\d{3}-\d{4}-\d{4}$/.test(newPhone)) {
      td.innerText = beforePhone; // 기존 번호로 되돌림
      return;
    }

  // newPhone이 기존 번호와 동일한 경우 변경하지 않음
  if (newPhone === beforePhone) {
    td.innerText = beforePhone; // 기존 번호로 되돌림
    return;
  }

  // 한 번더 확인
  if (!confirm(`정말로 ${beforePhone}을(를) ${newPhone}(으)로 변경하시겠습니까?`)) {
    td.innerText = beforePhone; // 기존 번호로 되돌림
    return;
  }

  fetch(`/admin/friends/${userId}`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json'
    },
    body: JSON.stringify({ phone: newPhone, lineId: lineId })
  })
  .then(async (response) => {
  if (!response.ok) {
    const error = await response.json();
    throw new Error(error.message || '서버 에러');
  }
    td.innerText = newPhone;
    td.setAttribute('data-phone-number', newPhone);
  })
  .catch(error => {
    alert('변경 중 에러 발생 : ' + error.message);
    td.innerText = beforePhone;
  });
}