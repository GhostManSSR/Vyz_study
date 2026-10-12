package BugBug.androidApp.data.local;

import BugBug.androidApp.model.Gender;
import android.database.Cursor;
import android.os.CancellationSignal;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.CoroutinesRoom;
import androidx.room.EntityDeletionOrUpdateAdapter;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import java.lang.Class;
import java.lang.Exception;
import java.lang.IllegalStateException;
import java.lang.Long;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import javax.annotation.processing.Generated;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import kotlinx.coroutines.flow.Flow;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class PlayerDao_Impl implements PlayerDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<PlayerEntity> __insertionAdapterOfPlayerEntity;

  private final Converters __converters = new Converters();

  private final EntityInsertionAdapter<ScoreEntity> __insertionAdapterOfScoreEntity;

  private final EntityDeletionOrUpdateAdapter<PlayerEntity> __deletionAdapterOfPlayerEntity;

  public PlayerDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfPlayerEntity = new EntityInsertionAdapter<PlayerEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR ABORT INTO `players` (`id`,`fullName`,`gender`,`course`,`difficulty`,`birthDate`,`zodiacName`,`createdAt`) VALUES (nullif(?, 0),?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final PlayerEntity entity) {
        statement.bindLong(1, entity.getId());
        statement.bindString(2, entity.getFullName());
        final String _tmp = __converters.genderToString(entity.getGender());
        if (_tmp == null) {
          statement.bindNull(3);
        } else {
          statement.bindString(3, _tmp);
        }
        statement.bindLong(4, entity.getCourse());
        statement.bindLong(5, entity.getDifficulty());
        final Long _tmp_1 = __converters.calendarToLong(entity.getBirthDate());
        if (_tmp_1 == null) {
          statement.bindNull(6);
        } else {
          statement.bindLong(6, _tmp_1);
        }
        statement.bindString(7, entity.getZodiacName());
        statement.bindLong(8, entity.getCreatedAt());
      }
    };
    this.__insertionAdapterOfScoreEntity = new EntityInsertionAdapter<ScoreEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR ABORT INTO `scores` (`id`,`playerId`,`score`,`hits`,`misses`,`difficulty`,`roundDurationSec`,`playedAt`) VALUES (nullif(?, 0),?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final ScoreEntity entity) {
        statement.bindLong(1, entity.getId());
        statement.bindLong(2, entity.getPlayerId());
        statement.bindLong(3, entity.getScore());
        statement.bindLong(4, entity.getHits());
        statement.bindLong(5, entity.getMisses());
        statement.bindLong(6, entity.getDifficulty());
        statement.bindLong(7, entity.getRoundDurationSec());
        statement.bindLong(8, entity.getPlayedAt());
      }
    };
    this.__deletionAdapterOfPlayerEntity = new EntityDeletionOrUpdateAdapter<PlayerEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "DELETE FROM `players` WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final PlayerEntity entity) {
        statement.bindLong(1, entity.getId());
      }
    };
  }

  @Override
  public Object insertPlayer(final PlayerEntity player,
      final Continuation<? super Long> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Long>() {
      @Override
      @NonNull
      public Long call() throws Exception {
        __db.beginTransaction();
        try {
          final Long _result = __insertionAdapterOfPlayerEntity.insertAndReturnId(player);
          __db.setTransactionSuccessful();
          return _result;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object insertScore(final ScoreEntity score, final Continuation<? super Long> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Long>() {
      @Override
      @NonNull
      public Long call() throws Exception {
        __db.beginTransaction();
        try {
          final Long _result = __insertionAdapterOfScoreEntity.insertAndReturnId(score);
          __db.setTransactionSuccessful();
          return _result;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object deletePlayer(final PlayerEntity player,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __deletionAdapterOfPlayerEntity.handle(player);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object getPlayerByName(final String name,
      final Continuation<? super PlayerEntity> $completion) {
    final String _sql = "SELECT * FROM players WHERE fullName = ? LIMIT 1";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, name);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<PlayerEntity>() {
      @Override
      @Nullable
      public PlayerEntity call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfFullName = CursorUtil.getColumnIndexOrThrow(_cursor, "fullName");
          final int _cursorIndexOfGender = CursorUtil.getColumnIndexOrThrow(_cursor, "gender");
          final int _cursorIndexOfCourse = CursorUtil.getColumnIndexOrThrow(_cursor, "course");
          final int _cursorIndexOfDifficulty = CursorUtil.getColumnIndexOrThrow(_cursor, "difficulty");
          final int _cursorIndexOfBirthDate = CursorUtil.getColumnIndexOrThrow(_cursor, "birthDate");
          final int _cursorIndexOfZodiacName = CursorUtil.getColumnIndexOrThrow(_cursor, "zodiacName");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final PlayerEntity _result;
          if (_cursor.moveToFirst()) {
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpFullName;
            _tmpFullName = _cursor.getString(_cursorIndexOfFullName);
            final Gender _tmpGender;
            final String _tmp;
            if (_cursor.isNull(_cursorIndexOfGender)) {
              _tmp = null;
            } else {
              _tmp = _cursor.getString(_cursorIndexOfGender);
            }
            final Gender _tmp_1 = __converters.stringToGender(_tmp);
            if (_tmp_1 == null) {
              throw new IllegalStateException("Expected NON-NULL 'BugBug.androidApp.model.Gender', but it was NULL.");
            } else {
              _tmpGender = _tmp_1;
            }
            final int _tmpCourse;
            _tmpCourse = _cursor.getInt(_cursorIndexOfCourse);
            final int _tmpDifficulty;
            _tmpDifficulty = _cursor.getInt(_cursorIndexOfDifficulty);
            final Calendar _tmpBirthDate;
            final Long _tmp_2;
            if (_cursor.isNull(_cursorIndexOfBirthDate)) {
              _tmp_2 = null;
            } else {
              _tmp_2 = _cursor.getLong(_cursorIndexOfBirthDate);
            }
            final Calendar _tmp_3 = __converters.longToCalendar(_tmp_2);
            if (_tmp_3 == null) {
              throw new IllegalStateException("Expected NON-NULL 'java.util.Calendar', but it was NULL.");
            } else {
              _tmpBirthDate = _tmp_3;
            }
            final String _tmpZodiacName;
            _tmpZodiacName = _cursor.getString(_cursorIndexOfZodiacName);
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            _result = new PlayerEntity(_tmpId,_tmpFullName,_tmpGender,_tmpCourse,_tmpDifficulty,_tmpBirthDate,_tmpZodiacName,_tmpCreatedAt);
          } else {
            _result = null;
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @Override
  public Flow<List<PlayerEntity>> getAllPlayers() {
    final String _sql = "SELECT * FROM players ORDER BY createdAt DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"players"}, new Callable<List<PlayerEntity>>() {
      @Override
      @NonNull
      public List<PlayerEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfFullName = CursorUtil.getColumnIndexOrThrow(_cursor, "fullName");
          final int _cursorIndexOfGender = CursorUtil.getColumnIndexOrThrow(_cursor, "gender");
          final int _cursorIndexOfCourse = CursorUtil.getColumnIndexOrThrow(_cursor, "course");
          final int _cursorIndexOfDifficulty = CursorUtil.getColumnIndexOrThrow(_cursor, "difficulty");
          final int _cursorIndexOfBirthDate = CursorUtil.getColumnIndexOrThrow(_cursor, "birthDate");
          final int _cursorIndexOfZodiacName = CursorUtil.getColumnIndexOrThrow(_cursor, "zodiacName");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final List<PlayerEntity> _result = new ArrayList<PlayerEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final PlayerEntity _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpFullName;
            _tmpFullName = _cursor.getString(_cursorIndexOfFullName);
            final Gender _tmpGender;
            final String _tmp;
            if (_cursor.isNull(_cursorIndexOfGender)) {
              _tmp = null;
            } else {
              _tmp = _cursor.getString(_cursorIndexOfGender);
            }
            final Gender _tmp_1 = __converters.stringToGender(_tmp);
            if (_tmp_1 == null) {
              throw new IllegalStateException("Expected NON-NULL 'BugBug.androidApp.model.Gender', but it was NULL.");
            } else {
              _tmpGender = _tmp_1;
            }
            final int _tmpCourse;
            _tmpCourse = _cursor.getInt(_cursorIndexOfCourse);
            final int _tmpDifficulty;
            _tmpDifficulty = _cursor.getInt(_cursorIndexOfDifficulty);
            final Calendar _tmpBirthDate;
            final Long _tmp_2;
            if (_cursor.isNull(_cursorIndexOfBirthDate)) {
              _tmp_2 = null;
            } else {
              _tmp_2 = _cursor.getLong(_cursorIndexOfBirthDate);
            }
            final Calendar _tmp_3 = __converters.longToCalendar(_tmp_2);
            if (_tmp_3 == null) {
              throw new IllegalStateException("Expected NON-NULL 'java.util.Calendar', but it was NULL.");
            } else {
              _tmpBirthDate = _tmp_3;
            }
            final String _tmpZodiacName;
            _tmpZodiacName = _cursor.getString(_cursorIndexOfZodiacName);
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            _item = new PlayerEntity(_tmpId,_tmpFullName,_tmpGender,_tmpCourse,_tmpDifficulty,_tmpBirthDate,_tmpZodiacName,_tmpCreatedAt);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Object getPlayerById(final long id, final Continuation<? super PlayerEntity> $completion) {
    final String _sql = "SELECT * FROM players WHERE id = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, id);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<PlayerEntity>() {
      @Override
      @Nullable
      public PlayerEntity call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfFullName = CursorUtil.getColumnIndexOrThrow(_cursor, "fullName");
          final int _cursorIndexOfGender = CursorUtil.getColumnIndexOrThrow(_cursor, "gender");
          final int _cursorIndexOfCourse = CursorUtil.getColumnIndexOrThrow(_cursor, "course");
          final int _cursorIndexOfDifficulty = CursorUtil.getColumnIndexOrThrow(_cursor, "difficulty");
          final int _cursorIndexOfBirthDate = CursorUtil.getColumnIndexOrThrow(_cursor, "birthDate");
          final int _cursorIndexOfZodiacName = CursorUtil.getColumnIndexOrThrow(_cursor, "zodiacName");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final PlayerEntity _result;
          if (_cursor.moveToFirst()) {
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpFullName;
            _tmpFullName = _cursor.getString(_cursorIndexOfFullName);
            final Gender _tmpGender;
            final String _tmp;
            if (_cursor.isNull(_cursorIndexOfGender)) {
              _tmp = null;
            } else {
              _tmp = _cursor.getString(_cursorIndexOfGender);
            }
            final Gender _tmp_1 = __converters.stringToGender(_tmp);
            if (_tmp_1 == null) {
              throw new IllegalStateException("Expected NON-NULL 'BugBug.androidApp.model.Gender', but it was NULL.");
            } else {
              _tmpGender = _tmp_1;
            }
            final int _tmpCourse;
            _tmpCourse = _cursor.getInt(_cursorIndexOfCourse);
            final int _tmpDifficulty;
            _tmpDifficulty = _cursor.getInt(_cursorIndexOfDifficulty);
            final Calendar _tmpBirthDate;
            final Long _tmp_2;
            if (_cursor.isNull(_cursorIndexOfBirthDate)) {
              _tmp_2 = null;
            } else {
              _tmp_2 = _cursor.getLong(_cursorIndexOfBirthDate);
            }
            final Calendar _tmp_3 = __converters.longToCalendar(_tmp_2);
            if (_tmp_3 == null) {
              throw new IllegalStateException("Expected NON-NULL 'java.util.Calendar', but it was NULL.");
            } else {
              _tmpBirthDate = _tmp_3;
            }
            final String _tmpZodiacName;
            _tmpZodiacName = _cursor.getString(_cursorIndexOfZodiacName);
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            _result = new PlayerEntity(_tmpId,_tmpFullName,_tmpGender,_tmpCourse,_tmpDifficulty,_tmpBirthDate,_tmpZodiacName,_tmpCreatedAt);
          } else {
            _result = null;
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @Override
  public Flow<List<ScoreRecord>> getTopScores(final int limit) {
    final String _sql = "\n"
            + "    SELECT p.fullName AS fullName,\n"
            + "           s.score AS score,\n"
            + "           s.difficulty AS difficulty,\n"
            + "           s.playedAt AS playedAt\n"
            + "    FROM scores s\n"
            + "    INNER JOIN players p ON p.id = s.playerId\n"
            + "    WHERE s.id = (\n"
            + "        SELECT s2.id FROM scores s2\n"
            + "        WHERE s2.playerId = s.playerId\n"
            + "        ORDER BY s2.score DESC, s2.playedAt ASC\n"
            + "        LIMIT 1\n"
            + "    )\n"
            + "    ORDER BY s.score DESC\n"
            + "    LIMIT ?\n";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, limit);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"scores",
        "players"}, new Callable<List<ScoreRecord>>() {
      @Override
      @NonNull
      public List<ScoreRecord> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfFullName = 0;
          final int _cursorIndexOfScore = 1;
          final int _cursorIndexOfDifficulty = 2;
          final int _cursorIndexOfPlayedAt = 3;
          final List<ScoreRecord> _result = new ArrayList<ScoreRecord>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final ScoreRecord _item;
            final String _tmpFullName;
            _tmpFullName = _cursor.getString(_cursorIndexOfFullName);
            final int _tmpScore;
            _tmpScore = _cursor.getInt(_cursorIndexOfScore);
            final int _tmpDifficulty;
            _tmpDifficulty = _cursor.getInt(_cursorIndexOfDifficulty);
            final long _tmpPlayedAt;
            _tmpPlayedAt = _cursor.getLong(_cursorIndexOfPlayedAt);
            _item = new ScoreRecord(_tmpFullName,_tmpScore,_tmpDifficulty,_tmpPlayedAt);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Flow<List<ScoreEntity>> getScoresForPlayer(final long playerId) {
    final String _sql = "\n"
            + "        SELECT * FROM scores\n"
            + "        WHERE playerId = ?\n"
            + "        ORDER BY playedAt DESC\n"
            + "    ";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, playerId);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"scores"}, new Callable<List<ScoreEntity>>() {
      @Override
      @NonNull
      public List<ScoreEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfPlayerId = CursorUtil.getColumnIndexOrThrow(_cursor, "playerId");
          final int _cursorIndexOfScore = CursorUtil.getColumnIndexOrThrow(_cursor, "score");
          final int _cursorIndexOfHits = CursorUtil.getColumnIndexOrThrow(_cursor, "hits");
          final int _cursorIndexOfMisses = CursorUtil.getColumnIndexOrThrow(_cursor, "misses");
          final int _cursorIndexOfDifficulty = CursorUtil.getColumnIndexOrThrow(_cursor, "difficulty");
          final int _cursorIndexOfRoundDurationSec = CursorUtil.getColumnIndexOrThrow(_cursor, "roundDurationSec");
          final int _cursorIndexOfPlayedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "playedAt");
          final List<ScoreEntity> _result = new ArrayList<ScoreEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final ScoreEntity _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final long _tmpPlayerId;
            _tmpPlayerId = _cursor.getLong(_cursorIndexOfPlayerId);
            final int _tmpScore;
            _tmpScore = _cursor.getInt(_cursorIndexOfScore);
            final int _tmpHits;
            _tmpHits = _cursor.getInt(_cursorIndexOfHits);
            final int _tmpMisses;
            _tmpMisses = _cursor.getInt(_cursorIndexOfMisses);
            final int _tmpDifficulty;
            _tmpDifficulty = _cursor.getInt(_cursorIndexOfDifficulty);
            final int _tmpRoundDurationSec;
            _tmpRoundDurationSec = _cursor.getInt(_cursorIndexOfRoundDurationSec);
            final long _tmpPlayedAt;
            _tmpPlayedAt = _cursor.getLong(_cursorIndexOfPlayedAt);
            _item = new ScoreEntity(_tmpId,_tmpPlayerId,_tmpScore,_tmpHits,_tmpMisses,_tmpDifficulty,_tmpRoundDurationSec,_tmpPlayedAt);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @NonNull
  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }
}
