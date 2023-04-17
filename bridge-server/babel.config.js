module.exports = {
  presets: [["@babel/preset-env", { targets: { node: "current" } }]],
  plugins: [
    "@babel/plugin-syntax-import-assertions",
    "@babel/plugin-syntax-dynamic-import",
    "@babel/plugin-syntax-top-level-await",
  ],
};
