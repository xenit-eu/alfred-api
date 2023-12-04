--[======================================================================[

latex-h-break.lua - Pandoc filter to get break after a level 4 or higher heading.
See https://stackoverflow.com/questions/21198025/pandoc-generation-of-pdf-from-markdown-4th-header-is-rendered-differently

Usage:

    $ pandoc --lua-filter latex-h-break.lua input.md -o output.pdf

--]======================================================================]

-- create it once, use it many times!
local hfill_block = pandoc.RawBlock('latex', '\\hfill')

function Header (elem)
    if 3 < elem.level then
        return { elem, hfill_block }
    else -- ignore headings at other levels!
        return nil
    end
end