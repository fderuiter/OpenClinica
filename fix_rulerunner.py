import re

# Fix RuleRunner
path = '/app/core/src/main/java/org/akaza/openclinica/logic/rulerunner/RuleRunner.java'
with open(path, 'r') as f:
    code = f.read()

code = code.replace(
'''    ExpressionService getExpressionService() {
        expressionService = this.expressionService != null ? expressionService : new ExpressionService(ds);
        return expressionService;
    }''',
'''    public void setExpressionService(ExpressionService expressionService) {
        this.expressionService = expressionService;
    }

    ExpressionService getExpressionService() {
        return expressionService;
    }'''
)
with open(path, 'w') as f:
    f.write(code)

# Fix RuleSetService to call setExpressionService
path2 = '/app/core/src/main/java/org/akaza/openclinica/service/rule/RuleSetService.java'
with open(path2, 'r') as f:
    code2 = f.read()

code2 = re.sub(r'(ruleRunner\.setDynamicsMetadataService\(dynamicsMetadataService\);)',
               r'\1\n        ruleRunner.setExpressionService(getExpressionService());', code2)
with open(path2, 'w') as f:
    f.write(code2)

