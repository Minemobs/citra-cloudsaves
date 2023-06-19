//TO EDIT
const API_URL = "https://localhost:8888"

onload = () => {
    const login = document.getElementById("login") as HTMLDialogElement;
    const register = login.cloneNode(true) as HTMLDialogElement;
    register.id = "register";
    login.parentElement!.append(register);
    (register.children[0]!.children[0]! as HTMLParagraphElement).textContent =
        "Register";
    register.getElementsByClassName("no-account")[0].remove();
};

const loginClicked = (registration: boolean) => {
    const dialog = document.getElementById(
        registration ? "register" : "login"
    ) as HTMLDialogElement;
    dialog.showModal();
};

const onSubmit = (event: SubmitEvent) => {
    const form = event.submitter!.parentElement! as HTMLFormElement;
    const dialog = event.submitter!.parentElement!.parentElement!
        .parentElement as HTMLDialogElement;
    // noinspection JSUnusedLocalSymbols
    const registration = dialog.id === "register";

    const usernameElement = form.children[0].children[1] as HTMLInputElement;
    const passwordElement = form.children[1].children[1] as HTMLInputElement;
    //TODO: send this to a DB
    usernameElement.value = "";
    passwordElement.value = "";
    dialog.close();
    return false;
};
