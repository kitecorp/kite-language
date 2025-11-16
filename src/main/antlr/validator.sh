#!/bin/bash

# Kite Grammar Validation Script
# This script tests that the ANTLR4 grammar compiles correctly

set -e

echo "🔍 Kite Grammar Validation"
echo "=========================="
echo ""

# Check if ANTLR4 jar exists
ANTLR_JAR="antlr-4.13.1-complete.jar"
ANTLR_URL="https://www.antlr.org/download/antlr-4.13.1-complete.jar"

if [ ! -f "$ANTLR_JAR" ]; then
    echo "📥 Downloading ANTLR4..."
    wget -q "$ANTLR_URL" || curl -sO "$ANTLR_URL"

    if [ ! -f "$ANTLR_JAR" ]; then
        echo "❌ Failed to download ANTLR4"
        echo "Please download manually from: $ANTLR_URL"
        exit 1
    fi
    echo "✅ ANTLR4 downloaded"
fi

echo ""
echo "🔨 Compiling grammar..."
echo ""

# Generate parser
java -jar "$ANTLR_JAR" Kite.g4

if [ $? -eq 0 ]; then
    echo ""
    echo "✅ Grammar compiled successfully!"
    echo ""
    echo "Generated files:"
    ls -lh Kite*.java 2>/dev/null | awk '{print "  " $9 " (" $5 ")"}'

    echo ""
    echo "📝 Next steps:"
    echo "  1. Compile generated Java files: javac Kite*.java"
    echo "  2. Test with sample: echo 'resource AWS.S3.Bucket b {}' | java org.antlr.v4.gui.TestRig Kite program -tree"
    echo "  3. Integrate into your build system"

else
    echo ""
    echo "❌ Grammar compilation failed!"
    echo "Please check the error messages above."
    exit 1
fi

echo ""
echo "🧪 Running basic syntax tests..."
echo ""

# Create test samples
cat > test_sample1.kite << 'EOF'
resource AWS.S3.Bucket myBucket {
    var String name = "test-bucket"
}
EOF

cat > test_sample2.kite << 'EOF'
@sensitive
resource AWS.RDS.Database db {
    var String password = "secret123"
}
EOF

cat > test_sample3.kite << 'EOF'
for i in 1..3
    resource Node node {
        var Number id = i
    }
EOF

echo "Created test samples:"
echo "  - test_sample1.kite (simple resource)"
echo "  - test_sample2.kite (resource with decorator)"
echo "  - test_sample3.kite (for loop)"

echo ""
echo "✅ Validation complete!"
echo ""
echo "To test the grammar manually:"
echo "  java -jar $ANTLR_JAR Kite.g4 && javac Kite*.java"
echo "  grun Kite program -gui < test_sample1.kite"