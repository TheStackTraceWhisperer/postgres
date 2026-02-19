# PostgreSQL Testcontainers - Quick Start

## ✅ H2 Replaced with PostgreSQL Testcontainers

All tests now run against a **real PostgreSQL 16 database** using Testcontainers.

---

## 🎯 What You Get

✅ **Real PostgreSQL 16** - Not H2 simulation  
✅ **Actual audit triggers** - PL/pgSQL triggers work  
✅ **Production-identical schema** - Same as production database  
✅ **Automatic setup** - No manual database configuration  
✅ **Full test coverage** - All 73 tests use real database  

---

## 🚀 Running Tests

### Prerequisites
- Docker must be running

### Run tests
```bash
cd /home/samuel/projects/postgres/application
mvn test
```

**First run:** Downloads postgres:16 image (~150MB, one-time)  
**Subsequent runs:** Reuses cached image (fast)

---

## 📁 Key Files

### Dependencies (pom.xml)
```xml
<testcontainers.version>1.19.3</testcontainers.version>

<!-- Dependencies -->
<dependency>
  <groupId>org.springframework.boot</groupId>
  <artifactId>spring-boot-testcontainers</artifactId>
</dependency>
<dependency>
  <groupId>org.testcontainers</groupId>
  <artifactId>postgresql</artifactId>
</dependency>
```

### Configuration
- `TestcontainersConfiguration.java` - Container setup
- `init-test-db.sql` - Database schema with triggers
- `application-test.properties` - Test properties

---

## 🧪 Test Classes

All tests now include:
```java
@Import(TestcontainersConfiguration.class)
```

**Updated:**
- ApplicationTests
- IntegrationTest
- AuditTriggerIntegrationTest
- WidgetRepositoryTest
- WidgetAuditRepositoryTest

---

## ✨ Why This Matters

### Before (H2)
- ❌ Simulated PostgreSQL
- ❌ No real trigger support
- ❌ Different SQL behavior

### After (Testcontainers)
- ✅ Real PostgreSQL 16
- ✅ Real PL/pgSQL triggers
- ✅ Production-identical behavior

---

## 📊 Performance

- **Container start:** ~3-5 seconds (once)
- **Container reuse:** Automatic across tests
- **Total test time:** ~10-15 seconds (was ~5-10s with H2)

**Worth it:** Slightly slower, but tests against real database!

---

## 🔍 Verify It's Working

While tests run:
```bash
docker ps | grep postgres
```

You should see:
```
testcontainers/postgres:16
```

---

## 🎉 Result

**All 73 tests now use real PostgreSQL!**

Including the 34 audit trigger tests that verify the actual database trigger functionality.

For complete details, see: `TESTCONTAINERS_MIGRATION.md`

