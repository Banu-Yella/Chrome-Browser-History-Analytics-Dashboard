export function buildCategoryData(tags = [], distribution = {}) {
  const categoryByName = new Map((Array.isArray(tags) ? tags : []).map((tag) => [tag.tag_Name, tag.category || 'Other']));
  const totals = new Map();
  Object.entries(distribution || {}).forEach(([tagName, count]) => { const category = categoryByName.get(tagName) || 'Other'; totals.set(category, (totals.get(category) || 0) + Number(count || 0)); });
  return [...totals.entries()].map(([name, value]) => ({ name, value })).sort((a, b) => b.value - a.value).slice(0, 10);
}
