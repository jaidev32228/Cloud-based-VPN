import { useState, useEffect } from "react";
import { Button } from "@/components/ui/button";
import { toggleDarkMode, initializeTheme } from "@/utils/theme-toggle";
import { Sun, Moon } from "lucide-react";

export function DarkModeToggle() {
  const [isDarkMode, setIsDarkMode] = useState(false);

  // Initialize theme on component mount
  useEffect(() => {
    initializeTheme();
    setIsDarkMode(document.documentElement.classList.contains("dark"));
  }, []);

  // Handle theme toggle
  const handleToggleDarkMode = () => {
    toggleDarkMode();
    // Update the state to force a re-render
    setIsDarkMode(document.documentElement.classList.contains("dark"));
  };

  return (
    <Button
      variant="ghost"
      size="icon"
      onClick={handleToggleDarkMode}
      className="text-foreground"
    >
      {isDarkMode ? (
        <Sun className="theme-icon sun" size={24} />
      ) : (
        <Moon className="theme-icon moon" size={24} />
      )}
    </Button>
  );
}