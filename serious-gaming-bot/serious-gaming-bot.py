import discord
from discord import SelectOption, TextChannel, Interaction, app_commands, CategoryChannel, Message
from discord.ui import Select, View
import logging
import os
import argparse
from typing import Optional, Dict
from dotenv import load_dotenv

def parse_arguments():
    parser = argparse.ArgumentParser(description='Discord Topic Channel Bot')
    parser.add_argument('--token', '-t',
                       default=os.environ.get('DISCORD_BOT_TOKEN'),
                       help='Discord bot token (can also be set via DISCORD_BOT_TOKEN environment variable)')
    parser.add_argument('--log-path', '-l',
                       default='/home/max/serious-gaming-bot/log/serious-gaming-bot.log',
                       help='Path to log file')
    args = parser.parse_args()
    
    if not args.token:
        parser.error("Token must be provided either via --token argument or DISCORD_BOT_TOKEN environment variable")
        
    return args

class TopicChannelSelect(Select):
    def __init__(self, channels: list[TextChannel]):
        # Create options from all channels in Topic Channels category
        options = [
            SelectOption(
                label=channel.name,
                value=str(channel.id),
                description=f"Show/hide {channel.name} channel"
            ) for channel in channels #sorted(channels, key=lambda c: c.name.lower())  # Sort channels alphabetically
        ]
        
        super().__init__(
            placeholder="Select channels to show...",
            min_values=0,
            max_values=len(options),
            options=options
        )

    async def callback(self, interaction: Interaction):
        await interaction.response.defer(ephemeral=True)
        member = interaction.user
        
        # Get all topic channels
        category = interaction.guild.get_channel(int(self.options[0].value)).category
        topic_channels = category.text_channels
        
        # Update permissions for all channels
        for channel in topic_channels:
            should_see = str(channel.id) in self.values
            await channel.set_permissions(member, view_channel=should_see)
        
        selected_channels = [channel.name for channel in topic_channels if str(channel.id) in self.values]
        if selected_channels:
            response = f"You can now see these channels: {', '.join(selected_channels)}"
        else:
            response = "You have hidden all topic channels"
            
        await interaction.followup.send(response, ephemeral=True)

class ChannelManagerView(View):
    def __init__(self, channels: list[TextChannel]):
        super().__init__(timeout=None)  # Make the view persistent
        self.add_item(TopicChannelSelect(channels))

class TopicChannelBot(discord.Client):
    def __init__(self):
        intents = discord.Intents.default()
        intents.message_content = True
        intents.guilds = True
        super().__init__(intents=intents)
        self.tree = app_commands.CommandTree(self)
        # Dictionary to store message IDs for each guild
        self.manager_messages: Dict[int, int] = {}

    async def setup_hook(self):
        # Register the command for all guilds the bot is in
        for guild in self.guilds:
            self.tree.copy_global_to(guild=guild)
            await self.tree.sync(guild=guild)

    async def on_ready(self):
        logger.info(f'Bot is ready and logged in as {self.user}!')
        await self.setup_channel_manager()

    def get_topic_channels(self, guild: discord.Guild) -> Optional[list[TextChannel]]:
        """Get all channels in the Topic Channels category."""
        category = discord.utils.get(guild.categories, name="Topic Channels")
        if category:
            return category.text_channels
        return None

    async def setup_channel_manager(self, guild: Optional[discord.Guild] = None):
        """Create or update the channel manager message."""
        guilds = [guild] if guild else self.guilds
        
        for guild in guilds:
            setup_channel = discord.utils.get(guild.text_channels, name="topic-channels")
            if not setup_channel:
                logger.error(f"Could not find topic-channels channel in guild {guild.name}")
                continue

            topic_channels = self.get_topic_channels(guild)
            if not topic_channels:
                logger.error(f"Could not find Topic Channels category in guild {guild.name}")
                continue

            try:
                # Try to find existing manager message
                existing_message = None
                if guild.id in self.manager_messages:
                    try:
                        existing_message = await setup_channel.fetch_message(self.manager_messages[guild.id])
                    except discord.NotFound:
                        pass

                view = ChannelManagerView(topic_channels)
                content = "**Topic Channel Manager**\nSelect which channels you want to see:"

                if existing_message:
                    # Update existing message
                    await existing_message.edit(content=content, view=view)
                    logger.info(f"Updated channel manager message in {guild.name}")
                else:
                    # Create new message if none exists
                    # First clean up any old messages
                    async for message in setup_channel.history():
                        await message.delete()
                    
                    # Send new message and store its ID
                    message = await setup_channel.send(content=content, view=view)
                    self.manager_messages[guild.id] = message.id
                    logger.info(f"Created new channel manager message in {guild.name}")

            except discord.Forbidden:
                logger.error(f"Missing permissions to manage messages in {guild.name}")
            except Exception as e:
                logger.error(f"Error updating channel manager in {guild.name}: {str(e)}")

    async def on_guild_channel_create(self, channel: discord.abc.GuildChannel):
        """Triggered when a channel is created."""
        if isinstance(channel, TextChannel) and channel.category and channel.category.name == "Topic Channels":
            logger.info(f"Channel {channel.name} created in Topic Channels category")
            await self.setup_channel_manager(channel.guild)

    async def on_guild_channel_delete(self, channel: discord.abc.GuildChannel):
        """Triggered when a channel is deleted."""
        if isinstance(channel, TextChannel) and channel.category and channel.category.name == "Topic Channels":
            logger.info(f"Channel {channel.name} deleted from Topic Channels category")
            await self.setup_channel_manager(channel.guild)

    async def on_guild_channel_update(self, before: discord.abc.GuildChannel, after: discord.abc.GuildChannel):
        """Triggered when a channel is updated (renamed, moved, etc)."""
        # Check if either the old or new channel is in Topic Channels category
        was_topic_channel = isinstance(before, TextChannel) and before.category and before.category.name == "Topic Channels"
        is_topic_channel = isinstance(after, TextChannel) and after.category and after.category.name == "Topic Channels"
        
        if was_topic_channel or is_topic_channel:
            # If the channel was moved in or out of Topic Channels, or renamed within Topic Channels
            if before.name != after.name or before.category != after.category:
                logger.info(f"Channel {before.name} updated in Topic Channels category")
                await self.setup_channel_manager(after.guild)
            # If the channel was moved inside the Topic Channels, update the Topic Channels message with the correct order
            if before.position != after.position:
                logger.info(f"Position of channel {before.name} in Topic Channels category was changed")
                await self.setup_channel_manager(after.guild)

def main():
    # Load environment variables from .env file
    load_dotenv()
    
    # Parse command line arguments
    args = parse_arguments()
    
    # Setup logging
    logging.basicConfig(
        level=logging.INFO,
        format='%(asctime)s - %(name)s - %(levelname)s - %(message)s',
        handlers=[
            logging.FileHandler(filename=args.log_path, encoding='utf-8', mode='w'),
            logging.StreamHandler()
        ]
    )
    global logger
    logger = logging.getLogger('serious-gaming-bot')

    try:
        bot = TopicChannelBot()
        bot.run(args.token)
    except Exception as e:
        logger.critical(f'Failed to start bot: {str(e)}', exc_info=True)

if __name__ == "__main__":
    main()