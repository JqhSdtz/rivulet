const {getESLintConfig} = require('@iceworks/spec');

const customConfig = {
    rules: {
        '@typescript-eslint/no-require-imports': 'off',
        '@iceworks/best-practices/recommend-functional-component': 'off'
    },
    extends: ['plugin:prettier/recommended', 'plugin:react-hooks/recommended']
};

// https://www.npmjs.com/package/@iceworks/spec
module.exports = getESLintConfig('react-ts', customConfig);
