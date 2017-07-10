package com.aidl;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.util.Log;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class StudentService extends Service {

    private static final String TAG = "StudentService";

    private CopyOnWriteArrayList<Student> students = new CopyOnWriteArrayList<>();
//    private CopyOnWriteArrayList<IStudentAddListener> listeners = new CopyOnWriteArrayList<>();
    private RemoteCallbackList<IStudentAddListener> listeners = new RemoteCallbackList<>();

    private StudentManager.Stub stub = new StudentManager.Stub() {
        @Override
        public List<Student> getStudents() throws RemoteException {
            Log.i(TAG, "getStudents()");
            return students;
        }

        @Override
        public void addStudent(Student s) throws RemoteException {
            Log.i(TAG, "addStudent()");
            students.add(s);
            onNewStudent(s);
        }

        @Override
        public void registerListener(IStudentAddListener listener) throws RemoteException {
            listeners.register(listener);
            Log.i(TAG, "listener register");
//            if (!listeners.contains(listener)) {
//                listeners.add(listener);
//            } else {
//                Log.i(TAG, "listener 已存在");
//            }
        }

        @Override
        public void unregisterListener(IStudentAddListener listener) throws RemoteException {
            listeners.unregister(listener);
            Log.i(TAG, "listener unregister");
//            if (listeners.contains(listener)) {
//                listeners.remove(listener);
//            } else {
//                Log.i(TAG, "listener 没有找到");
//            }
        }
    };

    /**
     * 增加学生之后，通知各个客户端
     */
    private void onNewStudent(Student student) {
        try {
            int N = listeners.beginBroadcast();
            for (int i = 0; i < N; i++) {
                listeners.getBroadcastItem(i).onStudentAdd(student);
            }
            listeners.finishBroadcast();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        testAddStudent();
        return stub;
    }

    /**
     * 新建一个线程，每隔5秒增加一名学生进行测试
     */
    private void testAddStudent() {
        new Thread() {
            @Override
            public void run() {
                try {
                    while (true) {
                        sleep(5000);
                        stub.addStudent(new Student("小明", 20));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }

}
