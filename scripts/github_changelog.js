#!/usr/bin/env node
const fs = require("node:fs");
const path = require("node:path");
const version = process.argv[2];
if (!version) {
  console.error("Missing required argument: version");
  process.exit(1);
}

// Find block with current version
const changelogPath = path.resolve(__dirname, "../CHANGELOG.md");
const changelog = fs.readFileSync(changelogPath, "utf-8");
const headingStart = "## ";
// Find block with current version
const block = changelog
  .split(headingStart)
  .find((block) => block.startsWith(`v${version}`))
  .trim();
let lines = block.split("\n");
// Remove version
lines.splice(0, 1);

console.log(lines.join("\n").trim());
