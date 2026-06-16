import os
import re

entity_dir = r"c:\Users\User\Desktop\Vortex-Admin-Pro\backend\vortex-admin-pro\src\main\java\com\vortexadmin\entity"

def get_table_name(content, class_name):
    match = re.search(r'@Table\(\s*name\s*=\s*"([^"]+)"', content)
    if match:
        return match.group(1)
    # Default to snake_case of class_name
    return re.sub(r'(?<!^)(?=[A-Z])', '_', class_name).lower()

def get_columns(content):
    columns = []
    # simplistic parsing: look for private Type name;
    # ignore fields with @OneToMany mappedBy or @ManyToMany mappedBy
    lines = content.split('\n')
    skip_next = False
    for i, line in enumerate(lines):
        if '@OneToMany' in line and 'mappedBy' in line:
            skip_next = True
            continue
        if '@ManyToMany' in line and 'mappedBy' in line:
            skip_next = True
            continue
            
        match = re.search(r'private\s+([A-Za-z0-9_<>]+)\s+([A-Za-z0-9_]+)\s*;', line)
        if match:
            if skip_next:
                skip_next = False
                continue
            col_type = match.group(1)
            col_name_camel = match.group(2)
            
            # check if it has @Column(name="...")
            col_name = re.sub(r'(?<!^)(?=[A-Z])', '_', col_name_camel).lower()
            if i > 0 and '@Column' in lines[i-1]:
                m = re.search(r'name\s*=\s*"([^"]+)"', lines[i-1])
                if m: col_name = m.group(1)
            elif i > 0 and '@JoinColumn' in lines[i-1]:
                m = re.search(r'name\s*=\s*"([^"]+)"', lines[i-1])
                if m: col_name = m.group(1)
            else:
                # if ManyToOne, typically it's col_name_id
                if i > 0 and ('@ManyToOne' in lines[i-1] or '@OneToOne' in lines[i-1]):
                    col_name = col_name + "_id"
                    
            # Skip id since it's auto incremented usually
            if col_name_camel == 'id':
                continue
                
            columns.append((col_name, col_type))
    return columns

with open("generate_all_tables.sql", "w", encoding='utf-8') as out:
    for file in os.listdir(entity_dir):
        if not file.endswith('.java'): continue
        class_name = file.replace('.java', '')
        with open(os.path.join(entity_dir, file), 'r', encoding='utf-8') as f:
            content = f.read()
            
        table_name = get_table_name(content, class_name)
        columns = get_columns(content)
        
        out.write(f"-- ==========================================\n")
        out.write(f"-- Table: {table_name}\n")
        out.write(f"-- ==========================================\n")
        out.write(f"DO $$\nDECLARE\n    uuid_val TEXT;\nBEGIN\n    FOR i IN 1..200 LOOP\n")
        out.write(f"        uuid_val := substr(gen_random_uuid()::text, 1, 8);\n")
        out.write(f"        INSERT INTO {table_name} (")
        col_names = [c[0] for c in columns]
        out.write(", ".join(col_names))
        out.write(") VALUES (\n            ")
        
        vals = []
        for c in columns:
            name, t = c
            if t in ('String', 'text'):
                vals.append(f"'{name}_' || i || '_' || uuid_val")
            elif t in ('Integer', 'Long', 'Double', 'BigDecimal'):
                vals.append("i")
            elif t == 'LocalDateTime' or t == 'Date':
                vals.append("CURRENT_TIMESTAMP")
            elif t == 'Boolean' or t == 'boolean':
                vals.append("true")
            elif t in ('User', 'Role', 'Organization', 'Team', 'Event', 'Task'): # object refs
                vals.append(f"(SELECT id FROM {get_table_name('', t)} ORDER BY random() LIMIT 1)")
            else:
                vals.append("NULL")
                
        out.write(",\n            ".join(vals))
        out.write("\n        ) ON CONFLICT DO NOTHING;\n")
        out.write(f"    END LOOP;\nEND $$;\n\n")

print("Generated generate_all_tables.sql")
