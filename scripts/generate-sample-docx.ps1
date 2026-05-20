# Generates sample .docx resumes (no Python required)
$ErrorActionPreference = "Stop"
$root = Split-Path -Parent (Split-Path -Parent $MyInvocation.MyCommand.Path)
$outDir = Join-Path $root "sample-data\resumes\docx"
New-Item -ItemType Directory -Force -Path $outDir | Out-Null

function Escape-Xml([string]$t) {
    return [System.Security.SecurityElement]::Escape($t)
}

function New-Paragraph([string]$text) {
    if ([string]::IsNullOrEmpty($text)) { return "<w:p/>" }
    $safe = Escape-Xml $text
    return "<w:p><w:r><w:t xml:space=`"preserve`">$safe</w:t></w:r></w:p>"
}

function Write-Docx([string]$path, [string[]]$blocks) {
    $body = ($blocks | ForEach-Object { New-Paragraph $_ }) -join ""
    $documentXml = @"
<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<w:document xmlns:w="http://schemas.openxmlformats.org/wordprocessingml/2006/main">
<w:body>$body<w:sectPr/></w:body>
</w:document>
"@
    $contentTypes = @"
<?xml version="1.0" encoding="UTF-8"?>
<Types xmlns="http://schemas.openxmlformats.org/package/2006/content-types">
  <Default Extension="rels" ContentType="application/vnd.openxmlformats-package.relationships+xml"/>
  <Default Extension="xml" ContentType="application/xml"/>
  <Override PartName="/word/document.xml" ContentType="application/vnd.openxmlformats-officedocument.wordprocessingml.document.main+xml"/>
</Types>
"@
    $rels = @"
<?xml version="1.0" encoding="UTF-8"?>
<Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">
  <Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument" Target="word/document.xml"/>
</Relationships>
"@
    $docRels = @"
<?xml version="1.0" encoding="UTF-8"?>
<Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships"/>
"@

    if (Test-Path $path) { Remove-Item $path -Force }
    Add-Type -AssemblyName System.IO.Compression
    Add-Type -AssemblyName System.IO.Compression.FileSystem
    $zip = [System.IO.Compression.ZipFile]::Open($path, [enum]::Parse([System.IO.Compression.ZipArchiveMode], 'Create'))
    function Add-Entry([string]$name, [string]$content) {
        $e = $zip.CreateEntry($name)
        $w = New-Object System.IO.StreamWriter($e.Open())
        $w.Write($content)
        $w.Close()
    }
    Add-Entry "[Content_Types].xml" $contentTypes
    Add-Entry "_rels/.rels" $rels
    Add-Entry "word/_rels/document.xml.rels" $docRels
    Add-Entry "word/document.xml" $documentXml
    $zip.Dispose()
}

$priya = @(
    "PRIYA SHARMA",
    "Email: priya.sharma@email.com | Phone: +91 98765 43210",
    "LinkedIn: https://www.linkedin.com/in/priyasharma-dev | Pune, India",
    "",
    "SUMMARY",
    "Senior software engineer with 6 years building enterprise Java applications and REST APIs. Experienced in Spring Boot, microservices, MySQL, and agile delivery for HR and fintech products.",
    "",
    "SKILLS",
    "Java, Spring Boot, Spring Security, Hibernate, SQL, MySQL, REST APIs, Microservices, Docker, Git, JUnit, Maven, React (basic)",
    "",
    "EXPERIENCE",
    "TechNova Solutions - Senior Java Developer (2021 - Present)",
    "• Designed Spring Boot microservices for employee onboarding and document workflows",
    "• Improved API latency by 35% through query tuning and caching",
    "• Led code reviews and mentored 3 junior engineers",
    "",
    "InfoCore Pvt Ltd - Java Developer (2019 - 2021)",
    "• Built REST integrations with third-party payroll systems",
    "",
    "EDUCATION",
    "B.Tech Computer Science, Pune University, 2019",
    "",
    "CERTIFICATIONS",
    "Oracle Certified Professional: Java SE 11 Developer",
    "AWS Certified Cloud Practitioner"
)

$alex = @(
    "ALEX KUMAR",
    "Email: alex.kumar@outlook.com | Phone: +1 415 555 0198",
    "LinkedIn: https://www.linkedin.com/in/alexkumar-java | San Francisco, CA",
    "",
    "PROFESSIONAL SUMMARY",
    "Backend engineer focused on scalable Java services, event-driven architecture, and cloud deployment on AWS. 7 years in product companies.",
    "",
    "TECHNICAL SKILLS",
    "Java 17, Spring Boot, Spring Data JPA, Kafka, PostgreSQL, SQL, Kubernetes, Docker, CI/CD, REST",
    "",
    "WORK HISTORY",
    "CloudHire Inc - Staff Engineer (2020 - Present)",
    "• Owned candidate matching service used by 200+ enterprise clients",
    "• Migrated monolith modules to Spring Boot microservices",
    "",
    "DataStack - Software Engineer (2017 - 2020)",
    "• Developed reporting APIs and batch ETL jobs in Java",
    "",
    "EDUCATION",
    "MS Computer Science, San Jose State University, 2017",
    "",
    "CERTIFICATIONS",
    "Certified Kubernetes Application Developer (CKAD)"
)

$jordan = @(
    "JORDAN LEE",
    "Email: jordan.lee@gmail.com | Phone: +91 99887 76655",
    "LinkedIn: https://www.linkedin.com/in/jordanlee-react | Bengaluru, India",
    "",
    "SUMMARY",
    "Frontend developer with 4 years creating responsive web apps with React, TypeScript, and modern CSS. Passionate about HR tech UX and accessible design.",
    "",
    "SKILLS",
    "React, TypeScript, JavaScript, HTML5, CSS3, Tailwind CSS, Vite, Axios, React Router, Jest, REST API integration, Git",
    "",
    "EXPERIENCE",
    "PeopleFirst HR - Frontend Engineer (2022 - Present)",
    "• Built recruiter dashboard with charts, filters, and dark mode",
    "• Integrated React SPA with Spring Boot backend using JWT auth",
    "",
    "WebCraft Studio - Junior Frontend Developer (2020 - 2022)",
    "• Implemented component library and form validation flows",
    "",
    "EDUCATION",
    "B.E. Information Science, RV College of Engineering, 2020",
    "",
    "CERTIFICATIONS",
    "Meta Front-End Developer Professional Certificate"
)

Write-Docx (Join-Path $outDir "priya-sharma-java.docx") $priya
Write-Docx (Join-Path $outDir "alex-kumar-backend.docx") $alex
Write-Docx (Join-Path $outDir "jordan-lee-frontend.docx") $jordan

Write-Host "Created sample DOCX files in $outDir"
