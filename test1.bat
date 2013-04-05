@echo off
FOR /L %%i IN (1,1,5) DO (
  Start java Customer localhost 2000 D 12 42
)