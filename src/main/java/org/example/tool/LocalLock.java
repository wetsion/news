package org.example.tool;

import org.jetbrains.annotations.NotNull;
import org.redisson.api.RFuture;
import org.redisson.api.RLock;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;

public class LocalLock implements RLock {
    @Override
    public String getName() {
        return null;
    }

    @Override
    public void lockInterruptibly(long leaseTime, TimeUnit unit) throws InterruptedException {

    }

    @Override
    public boolean tryLock(long waitTime, long leaseTime, TimeUnit unit) throws InterruptedException {
        return true;
    }

    @Override
    public void lock(long leaseTime, TimeUnit unit) {

    }

    @Override
    public boolean forceUnlock() {
        return false;
    }

    @Override
    public boolean isLocked() {
        return true;
    }

    @Override
    public boolean isHeldByThread(long threadId) {
        return false;
    }

    @Override
    public boolean isHeldByCurrentThread() {
        return true;
    }

    @Override
    public int getHoldCount() {
        return 0;
    }

    @Override
    public long remainTimeToLive() {
        return 0;
    }

    @Override
    public void lock() {

    }

    @Override
    public void lockInterruptibly() throws InterruptedException {

    }

    @Override
    public boolean tryLock() {
        return false;
    }

    @Override
    public boolean tryLock(long time, @NotNull TimeUnit unit) throws InterruptedException {
        return true;
    }

    @Override
    public void unlock() {

    }

    @NotNull
    @Override
    public Condition newCondition() {
        return null;
    }

    @Override
    public RFuture<Boolean> forceUnlockAsync() {
        return null;
    }

    @Override
    public RFuture<Void> unlockAsync() {
        return null;
    }

    @Override
    public RFuture<Void> unlockAsync(long threadId) {
        return null;
    }

    @Override
    public RFuture<Boolean> tryLockAsync() {
        return null;
    }

    @Override
    public RFuture<Void> lockAsync() {
        return null;
    }

    @Override
    public RFuture<Void> lockAsync(long threadId) {
        return null;
    }

    @Override
    public RFuture<Void> lockAsync(long leaseTime, TimeUnit unit) {
        return null;
    }

    @Override
    public RFuture<Void> lockAsync(long leaseTime, TimeUnit unit, long threadId) {
        return null;
    }

    @Override
    public RFuture<Boolean> tryLockAsync(long threadId) {
        return null;
    }

    @Override
    public RFuture<Boolean> tryLockAsync(long waitTime, TimeUnit unit) {
        return null;
    }

    @Override
    public RFuture<Boolean> tryLockAsync(long waitTime, long leaseTime, TimeUnit unit) {
        return null;
    }

    @Override
    public RFuture<Boolean> tryLockAsync(long waitTime, long leaseTime, TimeUnit unit, long threadId) {
        return null;
    }

    @Override
    public RFuture<Integer> getHoldCountAsync() {
        return null;
    }

    @Override
    public RFuture<Boolean> isLockedAsync() {
        return null;
    }

    @Override
    public RFuture<Long> remainTimeToLiveAsync() {
        return null;
    }
}
