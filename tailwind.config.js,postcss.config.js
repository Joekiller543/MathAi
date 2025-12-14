File: tailwind.config.js
```javascript
/** @type {import("tailwindcss").Config} */
module.exports = {
  "content": [
    "./src/main/resources/**/*.{html,js}",
    "./src/main/kotlin/**/*.{kt,kts}"
  ],
  "theme": {
    "extend": {}
  },
  "plugins": []
}
```

File: postcss.config.js
```javascript
module.exports = {
  "plugins": {
    "tailwindcss": {},
    "autoprefixer": {}
  }
}
```