Here is the `postcss.config.js` file required to configure the PostCSS CLI. This file registers the `tailwindcss` and `autoprefixer` plugins, enabling the `npm run build:css` script defined in your `package.json` to correctly compile your Tailwind directives into standard CSS.

File: postcss.config.js
```javascript
module.exports = {
  plugins: {
    tailwindcss: {},
    autoprefixer: {},
  },
}
```