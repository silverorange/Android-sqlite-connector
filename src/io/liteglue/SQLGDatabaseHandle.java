package io.liteglue;

/* package */ class SQLGDatabaseHandle implements SQLDatabaseHandle {
  public SQLGDatabaseHandle(String filename, int flags) {
    dbfilename = filename;
    openflags = flags;
  }

  @Override
  public int open() {
    /* check state (should be checked by caller): */
    if (dbfilename == null || dbhandle != 0) return SQLCode.MISUSE;

    SQLiteResponse response = SQLiteNative.sqlc_db_open(dbfilename, openflags);
    if (response.getResult() != SQLCode.OK) {
      return response.getResult();
    }

    dbhandle = response.getHandle();

    SQLiteNative.sqlc_handle_ct_delete(response);

    tokenizerContext = new SQLGTokenizerContextHandle();
    return tokenizerContext.register();
  }

  @Override
  public int keyNativeString(String key) {
    /* check state (should be checked by caller): */
    if (dbhandle == 0) return SQLCode.MISUSE;

    return SQLiteNative.sqlc_db_key_native_string(this.dbhandle, key);
  }

  @Override
  public int close() {
    /* check state (should be checked by caller): */
    if (dbhandle == 0) return SQLCode.MISUSE;

    if (tokenizerContext != null) {
      tokenizerContext.unregister();
      tokenizerContext = null;
    }

    return SQLiteNative.sqlc_db_close(this.dbhandle);
  }

  @Override
  public boolean isOpen() {
    return (dbhandle != 0);
  }

  @Override
  public SQLStatementHandle newStatementHandle(String sql) {
    /* check state (should be checked by caller): */
    if (dbhandle == 0) return null;

    return new SQLGStatementHandle(sql);
  }

  @Override
  public long getLastInsertRowid() {
    /* check state (should be checked by caller): */
    if (dbhandle == 0) return -1; /* illegit value */

    return SQLiteNative.sqlc_db_last_insert_rowid(dbhandle);
  }

  @Override
  public int getTotalChanges() {
    /* check state (should be checked by caller): */
    if (dbhandle == 0) return -1; /* illegit value */

    return SQLiteNative.sqlc_db_total_changes(dbhandle);
  }

  @Override
  public String getLastErrorMessage() {
    /* check state (should be checked by caller): */
    if (dbhandle == 0) return null; /* illegit value */
    return SQLiteNative.sqlc_db_errmsg_native(dbhandle);
  }

  private class SQLGTokenizerContextHandle implements SQLTokenizerContextHandle {
    public int register() {
      if (!registered) {
        SQLiteResponse response;

        response = SQLiteNative.sqlc_syn_context_create(dbhandle);
        if (response.getResult() != SQLCode.OK) {
          return response.getResult();
        }
        synonymContextHandle = response.getHandle();

        SQLiteNative.sqlc_handle_ct_delete(response);

        response = SQLiteNative.sqlc_stp_context_create(dbhandle);
        if (response.getResult() != SQLCode.OK) {
          SQLiteNative.sqlc_syn_context_delete(synonymContextHandle);
          return response.getResult();
        }

        stopwordsContextHandle = response.getHandle();

        SQLiteNative.sqlc_handle_ct_delete(response);

        SQLiteNative.sqlc_tokenizer_register_all(dbhandle, synonymContextHandle, stopwordsContextHandle);
  
        registered = true;  
      }
      return SQLCode.OK;
    }

    public void unregister() {
      if (!registered) {
        SQLiteNative.sqlc_syn_context_delete(synonymContextHandle);
        SQLiteNative.sqlc_stp_context_delete(stopwordsContextHandle);
        synonymContextHandle = 0;
        stopwordsContextHandle = 0;
        registered = false;
      }
    }

    private boolean registered = false;
    private long synonymContextHandle = 0;
    private long stopwordsContextHandle = 0;
  }

  // XXX TODO make this reusable:
  private class SQLGStatementHandle implements SQLStatementHandle {
    private SQLGStatementHandle(String sql) {
      this.sql = sql;
    }

    @Override
    public int prepare() {
      /* check state (should be checked by caller): */
      if (sql == null || sthandle != 0) return SQLCode.MISUSE;

      SQLiteResponse response = SQLiteNative.sqlc_db_prepare_st(dbhandle, sql);
      if (response.getResult() != SQLCode.OK) {
        return response.getResult();
      }

      sthandle = response.getHandle();

      SQLiteNative.sqlc_handle_ct_delete(response);

      return SQLCode.OK; /* 0 */
    }

    @Override
    public int bindDouble(int pos, double val) {
      /* check state (should be checked by caller): */
      if (sthandle == 0) return SQLCode.MISUSE;

      return SQLiteNative.sqlc_st_bind_double(this.sthandle, pos, val);
    }

    @Override
    public int bindInteger(int pos, int val) {
      /* check state (should be checked by caller): */
      if (sthandle == 0) return SQLCode.MISUSE;

      return SQLiteNative.sqlc_st_bind_int(this.sthandle, pos, val);
    }

    @Override
    public int bindLong(int pos, long val) {
      /* check state (should be checked by caller): */
      if (sthandle == 0) return SQLCode.MISUSE;

      return SQLiteNative.sqlc_st_bind_long(this.sthandle, pos, val);
    }

    @Override
    public int bindNull(int pos) {
      /* check state (should be checked by caller): */
      if (sthandle == 0) return SQLCode.MISUSE;

      return SQLiteNative.sqlc_st_bind_null(this.sthandle, pos);
    }

    @Override
    public int bindTextNativeString(int pos, String val) {
      /* check state (should be checked by caller): */
      if (sthandle == 0) return SQLCode.MISUSE;

      return SQLiteNative.sqlc_st_bind_text_native(this.sthandle, pos, val);
    }

    @Override
    public int step() {
      /* check state (should be checked by caller): */
      if (sthandle == 0) return SQLCode.MISUSE;

      return SQLiteNative.sqlc_st_step(this.sthandle);
    }

    @Override
    public int getColumnCount() {
      /* check state (should be checked by caller): */
      if (sthandle == 0) return -1;

      return SQLiteNative.sqlc_st_column_count(this.sthandle);
    }

    @Override
    public String getColumnName(int col) {
      /* check state (should be checked by caller): */
      if (sthandle == 0) return null;

      return SQLiteNative.sqlc_st_column_name(this.sthandle, col);
    }

    @Override
    public int getColumnType(int col) {
      /* check state (should be checked by caller): */
      if (sthandle == 0) return -1;

      return SQLiteNative.sqlc_st_column_type(this.sthandle, col);
    }

    @Override
    public double getColumnDouble(int col) {
      /* check state (should be checked by caller): */
      if (sthandle == 0) return -1;

      return SQLiteNative.sqlc_st_column_double(this.sthandle, col);
    }

    @Override
    public int getColumnInteger(int col) {
      /* check state (should be checked by caller): */
      if (sthandle == 0) return -1;

      return SQLiteNative.sqlc_st_column_int(this.sthandle, col);
    }

    @Override
    public long getColumnLong(int col) {
      /* check state (should be checked by caller): */
      if (sthandle == 0) return -1;

      return SQLiteNative.sqlc_st_column_long(this.sthandle, col);
    }

    @Override
    public String getColumnTextNativeString(int col) {
      /* check state (should be checked by caller): */
      if (sthandle == 0) return null;

      return SQLiteNative.sqlc_st_column_text_native(this.sthandle, col);
    }

    @Override
    public int finish() {
      /* check state (should be checked by caller): */
      if (sthandle == 0) return SQLCode.MISUSE;

      long mysthandle = sthandle;
      sql = null;
      sthandle = 0;

      return SQLiteNative.sqlc_st_finish(mysthandle);
    }

    String sql = null;
    private long sthandle = 0;
  }

  String dbfilename = null;
  int openflags = 0;
  private long dbhandle = 0;
  private SQLTokenizerContextHandle tokenizerContext = null;
}
