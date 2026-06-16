import os
import re

directory = r"C:\Users\User\Desktop\Vortex-Admin-Pro\backend\vortex-admin-pro\src\main\java\com\vortexadmin"

def process_file(filepath):
    with open(filepath, 'r', encoding='utf-8') as file:
        content = file.read()

    # Replacements
    content = content.replace("TenantContext.getCurrentUserId()", "SecurityUtils.getCurrentUserId()")
    content = content.replace("import com.vortexadmin.util.TenantContext;", "import com.vortexadmin.util.SecurityUtils;")
    content = content.replace("import com.vortexadmin.entity.Company;\n", "")
    content = content.replace("import com.vortexadmin.repository.CompanyRepository;\n", "")
    
    # Remove @Autowired CompanyRepository
    content = re.sub(r'@Autowired\s+private CompanyRepository companyRepository;', '', content)
    
    # Remove Company company = companyRepository.findById(TenantContext.getCurrentCompanyId()).get();
    content = re.sub(r'Company company = companyRepository\.findById\(TenantContext\.getCurrentCompanyId\(\)\)(\.orElseThrow\(\)|(?:\.get\(\)))?;', '', content)
    content = re.sub(r'Company company = companyRepository\.findById\(TenantContext\.getCurrentCompanyId\(\)\);', '', content)
    
    # Remove TenantContext.getCurrentCompanyId() assignments
    content = re.sub(r'Long companyId = TenantContext\.getCurrentCompanyId\(\);', '', content)
    content = content.replace(", TenantContext.getCurrentCompanyId()", "")
    
    # Remove if (!x.getCompany().getId().equals(TenantContext.getCurrentCompanyId()))
    content = re.sub(r'if\s*\(![^.]+\.getCompany\(\)\.getId\(\)\.equals\(TenantContext\.getCurrentCompanyId\(\)\)\)\s*\{[^}]+\}', '', content)

    # Some services passed company to builder, remove .company(company)
    content = re.sub(r'\.company\(company\)', '', content)

    with open(filepath, 'w', encoding='utf-8') as file:
        file.write(content)

for root, _, files in os.walk(directory):
    for file in files:
        if file.endswith(".java"):
            process_file(os.path.join(root, file))

print("Refactoring script completed.")
