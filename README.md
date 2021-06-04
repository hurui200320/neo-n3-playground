# neo-n3-playground

Test field for Neo N3 contract

## Introduce

This is the project used for testing Neo N3 contract codes.

By design, `master` branch is the template, each test code will make a new branch from master and focus on testing. All
common codes should be added in master. No testing code should appear in `master` branch.

Also, to detailed explain each bug, there should be an issue corresponding to each branch, and this readme file can be
changed to explain the code.

## Requirements

This repo makes use of [`neo-express`](https://github.com/neo-project/neo-express) to manage private chain. Beside
default genesis account, there are also test account `alice`, `bob` and `user`, all of those are defined
in `info.skyblond.demo.neo.env.DefaultAccount` class. Those 4 accounts (including genesis account) should be enough for
demonstration.

Note: You may need `dotnet tool install Neo.Express -g --version 2.0.32-preview+f8a192f373` to install RC2 version. The
default cmd is `neoxp`.
