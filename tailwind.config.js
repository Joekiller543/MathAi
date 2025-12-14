File: tailwind.config.js
```javascript
/** @type {import('tailwindcss').Config} */
module.exports = {
  content: [
    // Scan HTML and JavaScript files in the resources directory (e.g., static assets, templates)
    "./src/main/resources/**/*.{html,js}",
    
    // Scan Kotlin source files (including scripts) as they may contain HTML strings or DSLs with Tailwind classes
    "./src/main/kotlin/**/*.{kt,kts}"
  ],
  theme: {
    extend: {},
  },
  plugins: [],
}
```