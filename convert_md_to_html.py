import markdown
import os

# Paths
markdown_file = '/home/ubuntu/health_insurance_overview.md'
html_template_file = '/home/ubuntu/health-insurance-website/index.html.template' # Temp name
output_html_file = '/home/ubuntu/health-insurance-website/index.html'
placeholder = '<!-- Content will be inserted here -->'

# Rename original index.html to template
os.rename(output_html_file, html_template_file)

# Read markdown content
with open(markdown_file, 'r', encoding='utf-8') as f:
    md_content = f.read()

# Convert markdown to HTML with extensions for code blocks and tables
html_content = markdown.markdown(md_content, extensions=['fenced_code', 'tables', 'extra'])

# Read HTML template
with open(html_template_file, 'r', encoding='utf-8') as f:
    template_content = f.read()

# Insert HTML content into template
final_html = template_content.replace(placeholder, html_content)

# Write final HTML file
with open(output_html_file, 'w', encoding='utf-8') as f:
    f.write(final_html)

print(f'Successfully converted {markdown_file} to {output_html_file}')

