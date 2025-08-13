// src/utils/theme-toggle.ts
export function toggleDarkMode() {
  const htmlElement = document.documentElement;
  const isDarkMode = htmlElement.classList.toggle("dark");
  localStorage.setItem("theme", isDarkMode ? "dark" : "light");
}

export function initializeTheme() {
  const savedTheme = localStorage.getItem("theme");
  const prefersDarkMode = window.matchMedia("(prefers-color-scheme: dark)").matches;

  if (savedTheme === "dark" || (!savedTheme && prefersDarkMode)) {
    document.documentElement.classList.add("dark");
  } else {
    document.documentElement.classList.remove("dark");
  }
}