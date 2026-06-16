$files = Get-ChildItem "src\main\java\com\vortexadmin\service\impl" -Filter *.java

foreach ($file in $files) {
    $content = Get-Content $file.FullName -Raw
    
    $content = $content -replace "TenantContext\.getCurrentUserId\(\)", "SecurityUtils.getCurrentUserId()"
    $content = $content -replace "import com\.vortexadmin\.util\.TenantContext;", "import com.vortexadmin.util.SecurityUtils;"
    $content = $content -replace "import com\.vortexadmin\.entity\.Company;\r?\n", ""
    $content = $content -replace "import com\.vortexadmin\.repository\.CompanyRepository;\r?\n", ""
    $content = $content -replace "@Autowired\r?\n\s+private CompanyRepository companyRepository;\r?\n", ""
    $content = $content -replace "Company company = companyRepository\.findById\(TenantContext\.getCurrentCompanyId\(\)\)(\.orElseThrow\([^)]+\))?(\.get\(\))?;\r?\n", ""
    $content = $content -replace "Company company = companyRepository\.findById\(TenantContext\.getCurrentCompanyId\(\)\);\r?\n", ""
    $content = $content -replace "Long companyId = TenantContext\.getCurrentCompanyId\(\);\r?\n", ""
    $content = $content -replace ", TenantContext\.getCurrentCompanyId\(\)", ""
    $content = $content -replace "if \(!.*\.getCompany\(\)\.getId\(\)\.equals\(TenantContext\.getCurrentCompanyId\(\)\)\) \{\r?\n\s+throw new ApiException\(HttpStatus\.FORBIDDEN, `"Access Denied`"\);\r?\n\s+\}\r?\n", ""
    $content = $content -replace "\.company\(company\)", ""

    Set-Content -Path $file.FullName -Value $content -Encoding UTF8
}
