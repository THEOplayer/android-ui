#!/usr/bin/env node
const fs = require("node:fs");
const path = require("node:path");
const version = process.argv[2];
if (!version) {
  console.error("Missing required argument: version");
  process.exit(1);
}

// Update "version=1.2.3" in gradle.properties
const gradlePropertiesPath = path.resolve(__dirname, "../gradle.properties");
let gradleProperties = fs.readFileSync(gradlePropertiesPath, "utf8");
gradleProperties = gradleProperties.replace(
  /^version=.+$/m,
  `version=${version}`
);
fs.writeFileSync(gradlePropertiesPath, gradleProperties);

// Update heading in CHANGELOG.md
const changelogPath = path.resolve(__dirname, "../CHANGELOG.md");
let changelog = fs.readFileSync(changelogPath, "utf8");
const now = new Date();
const today = `${now.getFullYear()}-${(now.getMonth() + 1)
  .toString()
  .padStart(2, "0")}-${now.getDate().toString().padStart(2, "0")}`;
changelog = changelog.replace(
  /^## Unreleased$/m,
  `## $v{version} (${today})`
);
fs.writeFileSync(changelogPath, changelog);
