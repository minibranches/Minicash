package com.uramos.minicash.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.uramos.minicash.data.local.dao.AccountDao
import com.uramos.minicash.data.local.dao.BudgetDao
import com.uramos.minicash.data.local.dao.CategoryDao
import com.uramos.minicash.data.local.dao.CreditDao
import com.uramos.minicash.data.local.dao.CreditInstitutionDao
import com.uramos.minicash.data.local.dao.TransactionDao
import com.uramos.minicash.data.local.entities.AccountEntity
import com.uramos.minicash.data.local.entities.BudgetItemEntity
import com.uramos.minicash.data.local.entities.CategoryEntity
import com.uramos.minicash.data.local.entities.CreditEntity
import com.uramos.minicash.data.local.entities.CreditInstitutionEntity
import com.uramos.minicash.data.local.entities.TransactionEntity

val MIGRATION_3_4 = object : Migration(3, 4) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE credit_institutions ADD COLUMN colorHex TEXT NOT NULL DEFAULT '#1A73E8'")
    }
}

/**
 * Créditos pasan a referenciar el catálogo por id (FK, SET_NULL). Los nombres de institución que
 * existían solo como texto en créditos se agregan al catálogo y luego se enlazan por nombre.
 */
val MIGRATION_4_5 = object : Migration(4, 5) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            "INSERT INTO credit_institutions (name, colorHex) " +
                "SELECT DISTINCT TRIM(institution), '#1A73E8' FROM credits " +
                "WHERE TRIM(institution) <> '' " +
                "AND TRIM(institution) COLLATE NOCASE NOT IN (SELECT name FROM credit_institutions)"
        )
        db.execSQL(
            "CREATE TABLE credits_new (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                "article TEXT NOT NULL, " +
                "institution TEXT NOT NULL, " +
                "responsible TEXT NOT NULL, " +
                "purchaseDate INTEGER NOT NULL, " +
                "totalInstallments INTEGER NOT NULL, " +
                "paidInstallments INTEGER NOT NULL, " +
                "monthlyAmount INTEGER NOT NULL, " +
                "originalPrice INTEGER NOT NULL, " +
                "totalAmount INTEGER NOT NULL, " +
                "institutionId INTEGER, " +
                "FOREIGN KEY(institutionId) REFERENCES credit_institutions(id) " +
                "ON UPDATE NO ACTION ON DELETE SET NULL)"
        )
        db.execSQL(
            "INSERT INTO credits_new (id, article, institution, responsible, purchaseDate, totalInstallments, " +
                "paidInstallments, monthlyAmount, originalPrice, totalAmount, institutionId) " +
                "SELECT c.id, c.article, c.institution, c.responsible, c.purchaseDate, c.totalInstallments, " +
                "c.paidInstallments, c.monthlyAmount, c.originalPrice, c.totalAmount, " +
                "(SELECT ci.id FROM credit_institutions ci WHERE ci.name = TRIM(c.institution) COLLATE NOCASE LIMIT 1) " +
                "FROM credits c"
        )
        db.execSQL("DROP TABLE credits")
        db.execSQL("ALTER TABLE credits_new RENAME TO credits")
        db.execSQL("CREATE INDEX IF NOT EXISTS index_credits_institutionId ON credits (institutionId)")
    }
}

/** Día de pago y aviso de las compras a meses. */
val MIGRATION_5_6 = object : Migration(5, 6) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE credits ADD COLUMN paymentDay INTEGER")
        db.execSQL("ALTER TABLE credits ADD COLUMN reminderDaysBefore INTEGER")
    }
}

/** Día de pago y aviso en la institución; las compras que ya tenían día propio lo conservan. */
val MIGRATION_6_7 = object : Migration(6, 7) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE credit_institutions ADD COLUMN paymentDay INTEGER")
        db.execSQL("ALTER TABLE credit_institutions ADD COLUMN reminderDaysBefore INTEGER")
        db.execSQL("ALTER TABLE credits ADD COLUMN paymentOverride INTEGER NOT NULL DEFAULT 0")
        db.execSQL("UPDATE credits SET paymentOverride = 1 WHERE paymentDay IS NOT NULL")
    }
}

@Database(
    entities = [
        AccountEntity::class,
        CategoryEntity::class,
        TransactionEntity::class,
        BudgetItemEntity::class,
        CreditEntity::class,
        CreditInstitutionEntity::class
    ],
    version = 7,
    exportSchema = true
)
abstract class MinicashDatabase : RoomDatabase() {
    abstract fun accountDao(): AccountDao
    abstract fun categoryDao(): CategoryDao
    abstract fun transactionDao(): TransactionDao
    abstract fun budgetDao(): BudgetDao
    abstract fun creditDao(): CreditDao
    abstract fun creditInstitutionDao(): CreditInstitutionDao

    companion object {
        @Volatile
        private var INSTANCE: MinicashDatabase? = null

        fun getDatabase(context: android.content.Context): MinicashDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = androidx.room.Room.databaseBuilder(
                    context.applicationContext,
                    MinicashDatabase::class.java,
                    "minicash.db"
                )
                    .addMigrations(MIGRATION_3_4, MIGRATION_4_5, MIGRATION_5_6, MIGRATION_6_7)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
