import os
import glob

test_files = glob.glob("/app/core/src/test/java/org/akaza/openclinica/dao/**/*.java", recursive=True)

for file in test_files:
    if "DaoTest" in file:
        with open(file, 'r') as f:
            content = f.read()
        
        # Ensure EntityManager import
        if "import jakarta.persistence.EntityManager;" not in content:
            content = content.replace("import jakarta.persistence.Query;", "import jakarta.persistence.EntityManager;\nimport jakarta.persistence.Query;")
            
        # Ensure mockEntityManager is declared
        if "@Mock\n    private EntityManager mockEntityManager;" not in content and "EntityManager mockEntityManager;" not in content:
            content = content.replace("@Mock\n    private SessionFactory mockSessionFactory;", "@Mock\n    private EntityManager mockEntityManager;\n    @Mock\n    private SessionFactory mockSessionFactory;")
            
        # Fix mockSession -> mockEntityManager
        content = content.replace("when(mockSession)", "when(mockEntityManager)")
        content = content.replace("mockSession.", "mockEntityManager.")
        
        # Fix saveOrUpdate -> persist
        content = content.replace(".saveOrUpdate(", ".persist(")
        
        # Fix setString, setInteger -> setParameter
        content = content.replace(".setString(", ".setParameter(")
        content = content.replace(".setInteger(", ".setParameter(")
        
        with open(file, 'w') as f:
            f.write(content)

