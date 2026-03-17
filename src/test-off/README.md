# Test Configuration

This directory contains comprehensive tests for the Tweakeroo mod, specifically focusing on the `InventoryUtils.isBetterTool` method.

## Test Structure

- `InventoryUtilsTest.java` - Basic functionality tests covering core logic
- `InventoryUtilsAdvancedTest.java` - Advanced tests including performance and edge cases

## Running Tests

Run tests using Gradle:
```bash
./gradlew test
```

Run tests with verbose output:
```bash
./gradlew test --info
```

Run only InventoryUtils tests:
```bash
./gradlew test --tests "*InventoryUtilsTest*"
```

## Test Coverage

The tests cover:
- ✅ Basic tool vs no tool scenarios
- ✅ Material quality comparisons  
- ✅ Block-specific tool preferences
- ✅ Tool correctness logic
- ✅ Speed and efficiency comparisons
- ✅ Edge cases and null safety
- ✅ Durability considerations
- ✅ Performance testing
- ✅ Method stability and consistency

## Notes

Some tests may require additional mocking of Minecraft registry classes and configuration objects to run in a full test environment. The current implementation focuses on testing the core logic of the `isBetterTool` method.