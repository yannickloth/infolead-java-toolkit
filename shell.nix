#{ pkgs ? import <nixpkgs> {} }:
{ sources ? import ./nix/sources.nix
, pkgs ? import sources.nixpkgs {}
}:
pkgs.mkShell {
  allowUnfree = true;
  buildInputs = with pkgs; [
    # keep this line if you use bash
    pkgs.bashInteractive

    git
    jdk
    lorri
    maven
    niv
  ]; 
} 
