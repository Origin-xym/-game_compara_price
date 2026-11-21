/** 正则转义 */
function escapeReg(s) {
  return s.replace(/[.*+?^${}()|[\]\\]/g, '\\$&')
}

/**
 * 把文本切成 { text, hit } 片段，供模板渲染高亮。
 * 关键词内部空格按「任意空白」处理，实现 AC-2.1 的空格容忍。
 */
export function highlightParts(text, keyword) {
  const t = text || ''
  const kw = (keyword || '').trim()
  if (!kw) return [{ text: t, hit: false }]

  const pattern = kw.split(/\s+/).map(escapeReg).join('\\s*')
  const splitter = new RegExp(`(${pattern})`, 'gi')
  const tester = new RegExp(pattern, 'i')

  return t
    .split(splitter)
    .filter((p) => p !== '')
    .map((p) => ({ text: p, hit: tester.test(p) }))
}
