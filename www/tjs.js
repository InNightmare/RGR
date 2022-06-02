let pas = document.getElementById('password');
let mal = document.getElementById('letter');
let b = document.getElementById('capital');
let num = document.getElementById('number');
let sp = document.getElementById('special');
let len = document.getElementById('length');
let numbers = /[0-9]/;
let small = /[a-z]/;
let big = /[A-Z]/;
let spec = /\W/;

pas.onkeyup = () => {
    if (pas.value.match(numbers)) {
        num.style.color = "green";
    } else {
        num.style.color = "red";
    }
    if (pas.value.match(small)) {
        mal.style.color = "green";
    } else {
        mal.style.color = "red";
    }
    if (pas.value.match(big)) {
        b.style.color = "green";
    } else {
        b.style.color = "red";
    }
    if (pas.value.match(spec)) {
        sp.style.color = "green";
    } else {
        sp.style.color = "red";
    }
    if (pas.value.length >= 8) {
        len.style.color = "green";
    } else {
        len.style.color = "red";
    }
};
