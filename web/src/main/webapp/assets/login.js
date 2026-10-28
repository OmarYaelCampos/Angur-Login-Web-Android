(() => {
  "use strict";

  const form = document.getElementById("login-form");
  const identifier = document.getElementById("identifier");
  const password = document.getElementById("password");
  const button = document.getElementById("submit-button");
  const message = document.getElementById("form-message");
  const formView = document.getElementById("form-view");
  const menuView = document.getElementById("menu-view");

  function roleLabel(role) {
    const labels = {
      ADMINISTRADOR: "Administrador",
      VENDEDOR: "Vendedor",
      CLIENTE: "Cliente",
      SUPERVISOR_VENTAS: "Supervisor de ventas",
      ENCARGADO_ALMACEN: "Encargado de almacén",
    };
    return labels[role] || (role || "").toLowerCase().replaceAll("_", " ");
  }

  function clearErrors() {
    for (const input of [identifier, password]) {
      input.removeAttribute("aria-invalid");
      document.getElementById(`${input.id}-error`).textContent = "";
    }
    message.hidden = true;
    message.textContent = "";
  }

  function fieldError(input, text) {
    input.setAttribute("aria-invalid", "true");
    document.getElementById(`${input.id}-error`).textContent = text;
  }

  form.addEventListener("submit", async (event) => {
    event.preventDefault();
    clearErrors();

    let firstInvalid = null;
    if (!identifier.value.trim()) {
      fieldError(identifier, "Ingresa tu usuario o correo.");
      firstInvalid = identifier;
    }
    if (!password.value) {
      fieldError(password, "Ingresa tu contraseña.");
      firstInvalid ??= password;
    }
    if (firstInvalid) {
      message.textContent = "Debes llenar ambos campos: usuario y contraseña.";
      message.hidden = false;
      firstInvalid.focus();
      return;
    }

    if (window.location.protocol === "file:") {
      message.textContent = "Para comprobar credenciales, abre esta página desde Tomcat.";
      message.hidden = false;
      return;
    }

    button.disabled = true;
    button.textContent = "Validando…";
    try {
      const body = new URLSearchParams({
        identifier: identifier.value.trim(),
        password: password.value,
      });
      const response = await fetch(form.dataset.endpoint, {
        method: "POST",
        credentials: "same-origin",
        headers: { "Content-Type": "application/x-www-form-urlencoded;charset=UTF-8" },
        body,
      });
      const result = await response.json();
      if (!response.ok || !result.ok) {
        message.textContent = result.message || "No se pudo iniciar sesión.";
        message.hidden = false;
        password.value = "";
        password.focus();
        return;
      }
      password.value = "";
      const name = result.name || identifier.value.trim();
      document.getElementById("menu-name").textContent = name;
      document.getElementById("menu-greeting").textContent = name;
      document.getElementById("menu-role").textContent = roleLabel(result.role);
      document.getElementById("menu-initial").textContent = Array.from(name.trim())[0]?.toLocaleUpperCase("es-MX") || "A";
      formView.hidden = true;
      menuView.hidden = false;
    } catch (_) {
      message.textContent = "No fue posible conectarse al servicio. Inténtalo más tarde.";
      message.hidden = false;
    } finally {
      button.disabled = false;
      button.textContent = "Iniciar sesión";
    }
  });

  for (const input of [identifier, password]) {
    input.addEventListener("input", () => {
      input.removeAttribute("aria-invalid");
      document.getElementById(`${input.id}-error`).textContent = "";
      message.hidden = true;
    });
  }
})();
