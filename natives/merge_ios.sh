lipo lib$1.a.arm64 lib$1.a.arm7 lib$1.a.x86_64 lib$1.a.386 -create -output lib$1.a
rm lib$1.a.arm64 lib$1.a.arm7 lib$1.a.x86_64 lib$1.a.386

lipo lib$1.a.tvos.x86_64 lib$1.a.tvos.arm64 -create -output lib$1.a.tvos
rm lib$1.a.tvos.x86_64 lib$1.a.tvos.arm64

echo "merged libs for $1 "