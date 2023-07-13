const host = window.location.host;
const API_URL = `http${host.startsWith("localhost:") ? "" : "s"}://${host}/`;

type HttpErrorDetails = {
    code: number;
    message: string;
};

type HttpError = {
    title: string;
    status: number;
    type: string;
    details: HttpErrorDetails | {};
};

type HttpResponse = {
    message: string;
};

type Result = HttpResponse | HttpError | Blob;

onload = () => {
    const login = document.getElementById("login") as HTMLDialogElement;
    const register = login.cloneNode(true) as HTMLDialogElement;
    register.id = "register";
    login.parentElement!.append(register);
    (register.children[0]!.children[0]! as HTMLParagraphElement).textContent =
        "Register";
    register.getElementsByClassName("no-account")[0]?.remove();
};

function loginClicked(registration: boolean) {
    const dialog = document.getElementById(
        registration ? "register" : "login"
    ) as HTMLDialogElement;
    dialog.showModal();
}

async function handleAuthRequest(
    registration: boolean,
    username: string,
    password: string
) {
    let headers = new Headers();
    headers.append("username", username);
    headers.append("password", password);
    return await fetch(API_URL + (registration ? "register" : "login"), {
        method: "POST",
        headers: headers,
    })
        .then((res) => {
            if (!res.ok) return res.json();
            const contentType = res.headers.get("content-type");
            return !contentType ||
                contentType.indexOf("application/json") !== -1
                ? res.json()
                : res.blob();
        })
        .then((value) => value as Promise<Result>);
}

function onSubmit(event: SubmitEvent) {
    const form = event.submitter!.parentElement! as HTMLFormElement;
    const dialog = event.submitter!.parentElement!.parentElement!
        .parentElement as HTMLDialogElement;
    const errorElement = dialog.children[0]!.children[1]! as HTMLDivElement;
    const registration = dialog.id === "register";

    let usernameElement = (form.children[0]!.children[1] as HTMLInputElement)
        .value;
    let passwordElement = (form.children[1]!.children[1] as HTMLInputElement)
        .value;

    handleAuthRequest(registration, usernameElement, passwordElement)
        .then((res) => res as Exclude<Result, Blob>)
        .then((response) => {
            if ("status" in response) {
                let errorMessage = "";
                if ("code" in response.details)
                    errorMessage = response.details.message;
                else if (response.status == 429)
                    errorMessage = "Too many requests, try again in a minute";
                else errorMessage = response.title;
                errorElement.children[0]!.textContent = errorMessage;
                errorElement.style.display = "flex";
                return;
            }

            document.getElementById("token-div")!.style.display = "";
            document.getElementById("token")!.textContent = response.message;

            usernameElement = "";
            passwordElement = "";
            dialog.close();
            errorElement.style.display = "none";
        });
    return false;
}
