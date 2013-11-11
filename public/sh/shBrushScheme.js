SyntaxHighlighter.brushes.Scheme = function()
{
    // Contributed by Oscar Del Ben.
    
    var keywords = 'and begin call-with-current-continuation call-with-input-file call-with-output-file #t #f' +
                'case cond define define-syntax delay do dynamic-wind else for-each if lambda let let* let-syntax letrec' +
                'letrec-syntax map or syntax-rules #t #f';

    this.regexList = [
        { regex: /;(.*)$/gm,                                    css: 'comments' },// one line comments
        { regex: SyntaxHighlighter.regexLib.doubleQuotedString,css: 'string' },// double quoted strings
        { regex: SyntaxHighlighter.regexLib.singleQuotedString,css: 'string' },// single quoted strings
        { regex: new RegExp(this.getKeywords(keywords), 'gm'),css: 'keyword' },// keywords
        ];

};

SyntaxHighlighter.brushes.Scheme.prototype= new SyntaxHighlighter.Highlighter();
SyntaxHighlighter.brushes.Scheme.aliases= ['scheme'];
