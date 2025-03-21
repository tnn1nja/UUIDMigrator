cd target

del /Q "../../../Servers/guhcaDev/plugins\UUIDMigrator*"
for %%f in (UUIDMigrator*) do (
    copy "%%f" "../../../Servers/guhcaDev/plugins"
)

cd ..

echo File moved successfully.