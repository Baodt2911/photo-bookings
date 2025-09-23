/** @type {import('tailwindcss').Config} */

module.exports = {
  content: [
    "./src/main/resources/templates/**/*.html",
    "./src/main/resources/static/**/*.js",
  ],
    theme: {
        extend: {
            fontFamily: {
                'playfair': ['Playfair Display', 'serif'],
                'roboto': ['Roboto', 'sans-serif'],
            },
        },
    },
  plugins: [],
};
