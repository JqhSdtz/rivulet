const {getPrettierConfig} = require('@iceworks/spec');

const customConfig = {
    printWidth: 80,
    tabWidth: 4,
    useTabs: false,
    singleQuote: true,
    semi: true,
    trailingComma: 'none',
    bracketSpacing: false,
    arrowParens: 'avoid',
    jsxBracketSameLine: false,
    quoteProps: 'as-needed'
};

module.exports = getPrettierConfig('react', customConfig);
