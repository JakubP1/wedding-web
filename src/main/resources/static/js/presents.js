var http = new XMLHttpRequest();
http.onreadystatechange = function() {
  if (this.readyState == 4 && this.status == 200) {
    displayPresents(JSON.parse(this.responseText));
  }
}
http.open('GET', 'api/presents', true);
http.send();

$(document).ready(function() {
  var messages = pollMessages();
  for (var i=0; i < messages.length; i++) {
    displayMessage(messages[i]);
  }
});

function displayMessage(msg) {
  var message = $(
    '<div class="alert alert-success alert-dismissible" role="alert">' +
      '<button type="button" class="close" data-dismiss="alert">' +
        '<span aria-hidden="true">&times;</span>' +
      '</button>' +
      msg +
    '</div>'
  );
  $('#messages').append(message);
  message
    .delay(8000)
    .fadeOut(2000)
    .queue(function() {
      $(this).remove();
    });
}

function appendMessage(msg) {
  var messages = sessionStorage.getItem('messages');
  messages = JSON.parse(messages || '[]');
  messages.push(msg);
  sessionStorage.setItem('messages', JSON.stringify(messages));
}

function pollMessages() {
  var messages = sessionStorage.getItem('messages');
  sessionStorage.removeItem('messages');
  return JSON.parse(messages || '[]');
}

function displayPresents(presents) {
  var list = document.getElementById('presents');
  for (i=0; i < presents.length; i++) {
    var item = presentItem(presents[i]);
    list.appendChild(item);
  }
  window.presents = presents;
  console.log('adding autofocus handler...');
  $('#orderReservation').on('show.bs.modal', function() {
    console.log('autofocus...');
    document.activeElement.blur();
    $('#phoneNumber').focus();
//    $(this).find('[autofocus]').focus();
  });

}

function presentItem(present) {
    var item = document.createElement('div');
    item.className = 'present card ' + present.status.toLowerCase();
    item.innerHTML =
      '<a href="' + present.url + '" target="_blank">' +
      '<img class="card-img-top" src="' + present.imageUrl + '"/>' +
      '</a>' +
      '<div class="card-block">' +
      '<h4 class="card-title"><a href="' + present.url + '" target="_blank">' + present.title + '</a></h4>' +
      reservationButtonHtml(present) +
      '</div>'
    ;
    return item;
}

function reservationButtonHtml(present) {
  var disabled = '';
  var text = 'Rezervovat';
  var styleClass = ' btn-default ';
  if (present.status !== 'AVAILABLE') {
    disabled = " disabled='disable'";
    text = 'Rezervováno';
  }
  return '<button class="btn' + styleClass + 'orderReservation"' +
    disabled +
    ' onclick="orderReservation(' + present.id +')">' + text + '</button>';
}

function orderReservation(presentId) {
  var index = fetchPresentIndex(presentId);
  var present = window.presents[index];
  console.log(present);
  var card = $('article#main #presents > div')[index];
  $('#orderReservation #presentId').val(present.id);
  $('#orderReservation .modal-title').html('Rezervace: ' + present.title);
  $('#orderReservation #modalImage').attr('src', present.imageUrl);
  $('#orderReservation #modal-title-link').attr('href', present.url);
  $('#orderReservation #modal-image-link').attr('href', present.url);
  $('#orderReservation').modal('show');
}

function sendReservation(el) {
  var presentId = $('#orderReservation #presentId').val() >>> 0;
  var mobilePhone = $('#orderReservation #phoneNumber').val();
  var index = fetchPresentIndex(presentId);
  var present = window.presents[index];
  if (isValidPhone(mobilePhone)) {
    $.ajax({
      url: 'api/presents/' + presentId + '/reservations',
      type:"POST",
      data: JSON.stringify({ mobile: mobilePhone }, null, 2),
      contentType:"application/json; charset=utf-8",
      dataType:"json",
      success: function(){
        $('#orderReservation').modal('hide');
        appendMessage(
          'Rezervace daru "' + present.title + '" byla přijata ' +
          'a bude v blízké době telefonicky ověřena na čísle "' +
          mobilePhone + '".'
        );
        location.reload();
      }
    });
  }
}

function fetchPresentIndex(presentId) {
  console.log(presentId);
  var presents = window.presents;
  var len = presents.length;
  for (var index = 0; index < len; index++) {
    if (presents[index].id === presentId) {
      return index;
    }
  }
  return -1;
}

function isValidPhone(number) {
  return true;
}

