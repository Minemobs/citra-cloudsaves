//TO EDIT
//TODO: Put that back to https
const API_URL = "http://localhost:8888/";

const isNotNull = <T>(
    obj: T,
    callback: (obj: NonNullable<T>) => void,
    errorMessage: string
) => {
    if (obj == null) throw new Error(errorMessage);
    callback(obj);
};

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

const handleRequest = async (
    registration: boolean,
    username: string,
    password: string
) => {
    let headers = new Headers();
    headers.append("username", username);
    headers.append("password", password);
    return await fetch(API_URL + (registration ? "register" : "login"), {
        method: "POST",
        headers: headers,
    });
};

const onSubmit = (event: SubmitEvent) => {
    const form = event.submitter!.parentElement! as HTMLFormElement;
    const dialog = event.submitter!.parentElement!.parentElement!
        .parentElement as HTMLDialogElement;
    const registration = dialog.id === "register";

    const usernameElement = form.children[0].children[1] as HTMLInputElement;
    const passwordElement = form.children[1].children[1] as HTMLInputElement;

    handleRequest(registration, usernameElement.value, passwordElement.value)
        .then((response) => {
            //TODO: handle HTTP error codes
            isNotNull(
                document.getElementById("token-div"),
                (div) => (div.style.display = ""),
                "Element token-div is null"
            );
            isNotNull(
                document.getElementById("token"),
                async (p) => (p.textContent = await response.text()),
                "Element token is null"
            );
            usernameElement.value = "";
            passwordElement.value = "";
            dialog.close();
        })
        .catch(() => {});
    return false;
};
