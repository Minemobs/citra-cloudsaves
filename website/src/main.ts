onload = () => {
    const login = document.getElementById("login") as HTMLDialogElement;
    const register = login.cloneNode(true) as HTMLDialogElement;
    register.id = "register";
    login.parentElement!.append(register);
    (register.children[0]!.children[0]! as HTMLParagraphElement).textContent =
        "Register";
};

const loginClicked = (registration: boolean) => {
    const dialog = document.getElementById(
        registration ? "register" : "login"
    ) as HTMLDialogElement;
    dialog.open = true;
};

const onSubmit = (event: SubmitEvent) => {
    const form = event.submitter!.parentElement! as HTMLFormElement;
    const dialog = event.submitter!.parentElement!.parentElement!
        .parentElement as HTMLDialogElement;
    const registration = dialog.id === "register";

    const usernameElement = form.children[0].children[1] as HTMLInputElement;
    const passwordElement = form.children[1].children[1] as HTMLInputElement;
    //TODO: send this to a DB
    hashPassword(passwordElement.value).then(hash => {
        const username = usernameElement.value;
        console.log(hash);
    }).finally(() => {
        usernameElement.value = "";
        passwordElement.value = "";
        dialog.open = false;
    });
    return false;
};

const hashPassword = async (password: string) => {
    const utf8 = new TextEncoder().encode(password);
    const hashBuffer = await crypto.subtle.digest("SHA-512", utf8);
    const hashArray = Array.from(new Uint8Array(hashBuffer));
    const hashHex = hashArray
        .map((bytes) => bytes.toString(16).padStart(2, "0"))
        .join("");
    return hashHex;
}
