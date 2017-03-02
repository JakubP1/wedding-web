var http = new XMLHttpRequest();
http.onreadystatechange = function() {
  if (this.readyState == 4 && this.status == 200) {
    displayPresents(JSON.parse(this.responseText));
  }
}
http.open('GET', 'api/presents', true);
http.send();

function displayPresents(presents) {
  var list = document.getElementById('presents');
  for (i=0; i < presents.length; i++) {
    var item = presentItem(presents[i]);
    list.appendChild(item);
  }
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
  console.log('reserve...' + presentId);
}

