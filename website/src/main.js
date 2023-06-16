"use strict";
onload = () => {
    const login = document.getElementById("login");
    const register = login.cloneNode(true);
    register.id = "register";
    login.parentElement.append(register);
    register.children[0].children[0].textContent =
        "Register";
};
const loginClicked = (registration) => {
    const dialog = document.getElementById(registration ? "register" : "login");
    dialog.open = true;
};
const onSubmit = (event) => {
    const form = event.submitter.parentElement;
    const dialog = event.submitter.parentElement.parentElement
        .parentElement;
    const registration = dialog.id === "register";
    const usernameElement = form.children[0].children[1];
    const passwordElement = form.children[1].children[1];
    //TODO: send this to a DB
    console.log(usernameElement.value);
    console.log(passwordElement.value);
    usernameElement.value = "";
    passwordElement.value = "";
    dialog.open = false;
    return false;
};
function hash(password) {
    const utf8 = new TextEncoder().encode(password);
    return crypto.subtle.digest("SHA-512", utf8).then((hashBuffer) => {
        const hashArray = Array.from(new Uint8Array(hashBuffer));
        const hashHex = hashArray
            .map((bytes) => bytes.toString(16).padStart(2, "0"))
            .join("");
        return hashHex;
    });
}
